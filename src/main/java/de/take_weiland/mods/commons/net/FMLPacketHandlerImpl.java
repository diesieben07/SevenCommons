package de.take_weiland.mods.commons.net;

import com.google.common.collect.MapMaker;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataOutput;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

final class FMLPacketHandlerImpl<TYPE extends Enum<TYPE>> implements IPacketHandler, PacketFactory<TYPE>, PacketFactoryInternal<TYPE> {

	final String channel;
	private final PacketHandler<TYPE> handler;
	private final Class<TYPE> typeClass;
	final IdSize idSize;
	private Map<INetworkManager, EnumMap<TYPE, byte[][]>> partTracker;

	FMLPacketHandlerImpl(String channel, PacketHandler<TYPE> handler, Class<TYPE> typeClass) {
		this.channel = channel;
		this.handler = handler;
		this.typeClass = typeClass;
		this.idSize = IdSize.forCount(JavaUtils.getEnumConstantsShared(typeClass).length);
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
		return new Packet250FakeRaw<TYPE>(buf, this, buf.type);
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

}
