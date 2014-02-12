package de.take_weiland.mods.commons.net;

import com.google.common.collect.MapMaker;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.MiscUtil;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

final class FMLPacketHandlerImpl<TYPE extends Enum<TYPE>> implements IPacketHandler, PacketFactory<TYPE>, PacketFactoryInternal<TYPE> {

	private final String channel;
	private final PacketHandler<TYPE> handler;
	private final Class<TYPE> typeClass;
	final IdSize idSize;
	private Map<INetworkManager, EnumMap<TYPE, byte[][]>> partTracker;
	private final int MAX_SINGLE_SIZE;
	private final int MAX_PART_SIZE;
	
	FMLPacketHandlerImpl(String channel, PacketHandler<TYPE> handler, Class<TYPE> typeClass) {
		this.channel = channel;
		this.handler = handler;
		this.typeClass = typeClass;
		this.idSize = IdSize.forCount(JavaUtils.getEnumConstantsShared(typeClass).length);
		MAX_SINGLE_SIZE = Short.MAX_VALUE - 1 - idSize.byteSize; // bug in Packet250CustomPayload only allows Short.MAX_VALUE - 1 bytes
		MAX_PART_SIZE = MAX_SINGLE_SIZE - 2; // need two additional bytes for partIndex and partCount
		NetworkRegistry.instance().registerChannel(this, channel);
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fmlPlayer) {
		byte[] buf = packet.data;
		EntityPlayer player = (EntityPlayer) fmlPlayer;
		IdSize idSize = this.idSize;
		
		int id = idSize.readId(buf);
		TYPE t = JavaUtils.byOrdinal(typeClass, idSize.getActualId(id));
		if (idSize.isMultipart(id)) {
			handleMultipart(manager, buf, t, player);
		} else {
			DataBuf dataBuf = DataBuffers.newBuffer(buf);
			dataBuf.seek(idSize.byteSize);
			handle0(dataBuf, t, player);
		}
	}

	void handle0(DataBuf buf, TYPE t, EntityPlayer player) {
		handler.handle(t, buf, player, Sides.logical(player));
	}
	
	private void handleMultipart(INetworkManager manager, byte[] buf, TYPE t, EntityPlayer player) {
		int offset = idSize.byteSize;
		int partIndex = UnsignedBytes.checkedCast(buf[offset]);
		int partCount = UnsignedBytes.checkedCast(buf[offset + 1]);
		byte[][] parts = trackerFor(t, partCount, manager);
		parts[partIndex] = buf;
		int totalBytes = 0;
		for (byte[] part : parts) {
			if (part == null) {
				return;
			}
			totalBytes += part.length;
		}
		// we are complete
		killTracker(t, manager);
		byte[] all = new byte[totalBytes];
		int prefixLen = 2 + idSize.byteSize;
		int pos = 0;
		for (byte[] part : parts) {
			int len = part.length - prefixLen;
			System.arraycopy(part, prefixLen, all, pos, len);
			pos += len;
		}
		handle0(DataBuffers.newBuffer(all), t, player);
	}
	
	private static final MapMaker trackerMaker = new MapMaker().weakKeys().concurrencyLevel(2); // we have at most Client & Server thread
	private byte[][] trackerFor(TYPE t, int numParts, INetworkManager manager) {
		Map<INetworkManager, EnumMap<TYPE, byte[][]>> trackers = partTracker;
		if (trackers == null) {
			trackers = partTracker = trackerMaker.makeMap();
		}
		EnumMap<TYPE, byte[][]> tracker = trackers.get(manager);
		if (tracker == null) {
			tracker = new EnumMap<TYPE, byte[][]>(typeClass);
			trackers.put(manager, tracker);
		}
		byte[][] data = tracker.get(t);
		if (data == null) {
			data = new byte[numParts][];
			tracker.put(t, data);
		}
		return data;
	}
	
	private void killTracker(TYPE t, INetworkManager manager) {
		Map<INetworkManager, EnumMap<TYPE, byte[][]>> trackers = partTracker;
		if (trackers == null) {
			return;
		}
		EnumMap<TYPE, byte[][]> tracker = trackers.get(manager);
		if (tracker == null) {
			return;
		}
		tracker.remove(t);
	}

	@Override
	public PacketBuilder builder(TYPE t) {
		return builder0(t, -1); // -1 will force newWritable0 to pick the default capacity
	}
	
	@Override
	public PacketBuilder builder(TYPE t, int capacity) {
		return builder0(t, capacity);
	}
	
	private WritableDataBufImpl<TYPE> builder0(TYPE t, int capacity) {
		WritableDataBufImpl<TYPE> buf = DataBuffers.newWritable0(capacity);
		buf.packetFactory = this;
		buf.type = t;
		return buf;
	}
	
	// PacketFactoryInternal
	
	@Override
	public SimplePacket make(WritableDataBufImpl<TYPE> buf) {
		buf.seek(0);
		int size = buf.available();
		if (size <= MAX_SINGLE_SIZE) {
			return makeSingle(buf, buf.type);
		} else {
			return makeMulti(buf, buf.type);
		}
	}
	
	private SimplePacket makeSingle(WritableDataBufImpl<TYPE> buf, TYPE type) {
		return new SinglePacket250Fake<TYPE>(channel, type, buf, this);
	}
	
	private SimplePacket makeMulti(WritableDataBufImpl<TYPE> buf, TYPE type) {
		int avail = buf.available();
		int numParts = MathHelper.ceiling_float_int(avail / (float)MAX_PART_SIZE);
		Packet[] packets = new Packet[numParts];
		for (int i = 0; i < numParts; ++i) {
			int sizeOfPart = Math.min(avail, MAX_PART_SIZE);
			avail -= sizeOfPart;
			int offset = i * MAX_PART_SIZE;
			// only the first packet should execute on a MemoryConnection
			packets[i] = new MultiPacket250Fake<TYPE>(channel, buf, offset, sizeOfPart, type, i, numParts, this, i == 0);
		}
		return new MultiPacketWrapper(packets);
	}
	
	static {
		Map<Class<? extends Packet>, Integer> classToIdMap = MiscUtil.getReflector().getClassToIdMap(null);
		classToIdMap.put(MultiPacket250Fake.class, Integer.valueOf(250));
		classToIdMap.put(SinglePacket250Fake.class, Integer.valueOf(250));
	}
	
	static enum IdSize {
		
		BYTE_SUFFICIENT(1),
		SHORT_SUFFICIENT(2);
		
		final int byteSize;
		
		private IdSize(int byteSize) {
			this.byteSize = byteSize;
		}

		static IdSize forCount(int numIds) {
			if (numIds <= ~BYTE_MSB) {
				return BYTE_SUFFICIENT;
			} else if (numIds <= ~SHORT_MSB) {
				return SHORT_SUFFICIENT;
			} else {
				throw new IllegalArgumentException("Too many packets!");
			}
		}
		
		int readId(byte[] buf) {
			if (this == BYTE_SUFFICIENT) {
				return buf[0];
			} else {
				return Shorts.fromBytes(buf[0], buf[1]);
			}
		}
		
		void write(DataOutput out, int id, boolean multipart) throws IOException {
			if (this == BYTE_SUFFICIENT) {
				out.writeByte((id & ~BYTE_MSB) | (multipart ? BYTE_MSB : 0));
			} else {
				out.writeShort((id & ~BYTE_MSB) | (multipart ? SHORT_MSB : 0));
			}
		}
		
		private static final int BYTE_MSB = -128;
		private static final int SHORT_MSB = -32768;
		
		boolean isMultipart(int id) {
			if (this == BYTE_SUFFICIENT) {
				return (id & BYTE_MSB) == BYTE_MSB;
			} else {
				return (id & SHORT_MSB) == SHORT_MSB;
			}
		}
		
		int getActualId(int id) {
			if (this == BYTE_SUFFICIENT) {
				return id & ~BYTE_MSB;
			} else {
				return id & ~SHORT_MSB;
			}
		}
		
	}
	
	static class MultiPacket250Fake<TYPE extends Enum<TYPE>> extends Packet250CustomPayload {

		private int off;
		private int partIndex, totalParts;
		private TYPE type;
		private FMLPacketHandlerImpl<TYPE> ph;
		private WritableDataBufImpl<TYPE> buffer;
		private boolean isFirst;

		MultiPacket250Fake(String channel, WritableDataBufImpl<TYPE> buffer, int off, int len, TYPE type, int partIndex, int totalParts, FMLPacketHandlerImpl<TYPE> ph, boolean isFirst) {
			this.channel = channel;
			this.off = off;
			this.length = len;
			this.ph = ph;
			this.buffer = buffer;
			this.type = type;
			this.partIndex = partIndex;
			this.totalParts = totalParts;
			this.isFirst = isFirst;
		}

		@Override
		public void readPacketData(DataInput in) {
			throw new IllegalStateException("This should not happen!");
		}

		@Override
		public void writePacketData(DataOutput out) {
			try {
				writeString(channel, out);
				out.writeShort(length + ph.idSize.byteSize + 2);
				ph.idSize.write(out, type.ordinal(), true);
				out.writeByte(UnsignedBytes.checkedCast(partIndex));
				out.writeByte(UnsignedBytes.checkedCast(totalParts));
				out.write(buffer.buf, off, length);
			} catch (IOException e) {
				JavaUtils.throwUnchecked(e);
			}
		}

		@Override
		public void processPacket(NetHandler handler) {
			if (isFirst) {
				buffer.seek(0);
				ph.handle0(buffer, type, handler.getPlayer());
			}
		}

		@Override
		public int getPacketSize() {
			return 2 + channel.length() * 2 // channel name
					+ 2 // length
					+ ph.idSize.byteSize + 2 // packetId + partIndex + totalparts
					+ length; // actual data
		}
		
	}
	
	static class SinglePacket250Fake<TYPE extends Enum<TYPE>> extends Packet250CustomPayload implements SimplePacket {

		private TYPE type;
		private WritableDataBufImpl<TYPE> buf;
		private FMLPacketHandlerImpl<TYPE> fmlPh;
		
		SinglePacket250Fake(String channel, TYPE type, WritableDataBufImpl<TYPE> buf, FMLPacketHandlerImpl<TYPE> fmlPh) {
			this.channel = channel;
			this.type = type;
			this.buf = buf;
			this.length = buf.actualLen + 1;
			this.fmlPh = fmlPh;
		}

		@Override
		public void readPacketData(DataInput in) {
			throw new IllegalStateException("Unpossible!");
		}

		@Override
		public void writePacketData(DataOutput out) {
			try {
				writeString(channel, out);
				out.writeShort(length);
				fmlPh.idSize.write(out, type.ordinal(), false);
				out.write(buf.buf, 0, buf.actualLen);
			} catch (IOException e) {
				JavaUtils.throwUnchecked(e);
			}
		}

		@Override
		public void processPacket(NetHandler handler) {
			fmlPh.handle0(buf, type, handler.getPlayer());
		}

		// SimplePacket
		@Override
		public void sendTo(PacketTarget target) {
			target.send(this);
		}

		@Override
		public void sendToServer() {
			PacketDispatcher.sendPacketToServer(this);
		}

		@Override
		public void sendTo(EntityPlayer player) {
			Packets.sendPacketToPlayer(this, player);
		}

		@Override
		public void sendTo(Iterable<? extends EntityPlayer> players) {
			Packets.sendPacketToPlayers(this, players);
		}

		@Override
		public void sendToAll() {
			PacketDispatcher.sendPacketToAllPlayers(this);
		}

		@Override
		public void sendToAllInDimension(int dimension) {
			PacketDispatcher.sendPacketToAllInDimension(this, dimension);
		}

		@Override
		public void sendToAllInDimension(World world) {
			PacketDispatcher.sendPacketToAllInDimension(this, world.provider.dimensionId);
		}

		@Override
		public void sendToAllNear(World world, double x, double y, double z, double radius) {
			PacketDispatcher.sendPacketToAllAround(x, y, z, radius, world.provider.dimensionId, this);
		}

		@Override
		public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
			PacketDispatcher.sendPacketToAllAround(x, y, z, radius, dimension, this);
		}

		@Override
		public void sendToAllNear(Entity entity, double radius) {
			PacketDispatcher.sendPacketToAllAround(entity.posX, entity.posY, entity.posZ, radius, entity.worldObj.provider.dimensionId, this);
		}

		@Override
		public void sendToAllNear(TileEntity te, double radius) {
			PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, radius, te.worldObj.provider.dimensionId, this);
		}

		@Override
		public void sendToAllTracking(Entity entity) {
			Packets.sendPacketToAllTracking(this, entity);
		}

		@Override
		public void sendToAllTracking(TileEntity te) {
			Packets.sendPacketToAllTracking(this, te);
		}

		@Override
		public void sendToAllAssociated(Entity e) {
			Packets.sendPacketToAllAssociated(this, e);
		}

		@Override
		public void sendToViewing(Container c) {
			Packets.sendPacketToViewing(this, c);
		}
	}
	
	static class MultiPacketWrapper implements SimplePacket {

		private final Packet[] parts;
		
		MultiPacketWrapper(Packet[] parts) {
			this.parts = parts;
		}

		@Override
		public void sendTo(PacketTarget target) {
			for (Packet p : parts) {
				target.send(p);
			}
		}

		@Override
		public void sendToServer() {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToServer(p);
			}
		}

		@Override
		public void sendTo(EntityPlayer player) {
			for (Packet p : parts) {
				Packets.sendPacketToPlayer(p, player);
			}
		}

		@Override
		public void sendTo(Iterable<? extends EntityPlayer> players) {
			for (Packet p : parts) {
				Packets.sendPacketToPlayers(p, players);
			}
		}

		@Override
		public void sendToAll() {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllPlayers(p);
			}
		}

		@Override
		public void sendToAllInDimension(int dimension) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllInDimension(p, dimension);
			}
		}

		@Override
		public void sendToAllInDimension(World world) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllInDimension(p, world.provider.dimensionId);
			}
		}

		@Override
		public void sendToAllNear(World world, double x, double y, double z, double radius) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllAround(x, y, z, radius, world.provider.dimensionId, p);
			}
		}

		@Override
		public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllAround(x, y, z, radius, dimension, p);
			}
		}

		@Override
		public void sendToAllNear(Entity entity, double radius) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllAround(entity.posX, entity.posY, entity.posZ, radius, entity.worldObj.provider.dimensionId, p);
			}
		}

		@Override
		public void sendToAllNear(TileEntity te, double radius) {
			for (Packet p : parts) {
				PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, radius, te.worldObj.provider.dimensionId, p);
			}
		}

		@Override
		public void sendToAllTracking(Entity entity) {
			for (Packet p : parts) {
				Packets.sendPacketToAllTracking(p, entity);
			}
		}

		@Override
		public void sendToAllTracking(TileEntity te) {
			for (Packet p : parts) {
				Packets.sendPacketToAllTracking(p, te);
			}
		}

		@Override
		public void sendToAllAssociated(Entity e) {
			for (Packet p : parts) {
				Packets.sendPacketToAllAssociated(p, e);
			}
		}

		@Override
		public void sendToViewing(Container c) {
			for (Packet p : parts) {
				Packets.sendPacketToViewing(p, c);
			}
		}
	}
	
}
