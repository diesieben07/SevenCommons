package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.internal.SCPackets;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataOutput;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

final class FMLPacketHandlerImpl<TYPE extends Enum<TYPE>> implements IPacketHandler, PacketFactory<TYPE>, PacketFactoryInternal<TYPE> {

	final String channel;
	private final PacketHandler<TYPE> handler;
	private final Class<TYPE> typeClass;
	final int idSize;
	private final int responseId;

	FMLPacketHandlerImpl(String channel, PacketHandler<TYPE> handler, Class<TYPE> typeClass) {
		this.channel = checkNotNull(channel);
		this.handler = checkNotNull(handler);
		this.typeClass = checkNotNull(typeClass);
		int len = JavaUtils.getEnumConstantsShared(typeClass).length;
		responseId = len; // use next available ID as the response packet
		this.idSize = calcByteCount(len + 1);
		NetworkRegistry.instance().registerChannel(this, channel);
	}

	/**
	 * calculate the number of bytes needed to represent the given numer of packet types.
	 */
	private static int calcByteCount(int numPackets) {
		checkArgument(numPackets > 0, "Must have at least one packet type!");
		int numBytes = Integer.numberOfTrailingZeros(Integer.highestOneBit(numPackets)) / 8;
		return numBytes;
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fmlPlayer) {
		byte[] buf = packet.data;
		EntityPlayer player = (EntityPlayer) fmlPlayer;

		int id = readId(buf);

		TYPE t = JavaUtils.byOrdinal(typeClass, id);
		DataBufImpl dataBuf = DataBuffers.newBuffer0(buf);
		dataBuf.seek(idSize);
		handle0(dataBuf, t, player);
	}

	private int readId(byte[] buf) {
		int result = 0;
		for (int i = 0; i < idSize; ++i) {
			result |= buf[i] << (i << 3);
		}
		return result;
	}


	void write(DataOutput out, int id) throws IOException {
		for (int i = 0; i < idSize; ++i) {
			int shift = i << 3;
			out.writeByte((id & (0xFF << shift)) >> shift);
		}
	}

	void handle0(DataBufImpl buf, TYPE t, EntityPlayer player) {
		buf.factory = this;
		handler.handle(t, buf, player, Sides.logical(player));
	}

	@Override
	public PacketBuilder builder(TYPE t) {
		return builder0(t, -1); // -1 will make newWritable0 pick the default capacity
	}
	
	@Override
	public PacketBuilder builder(TYPE t, int capacity) {
		checkArgument(capacity > 0, "capacity must be > 0");
		return builder0(t, capacity);
	}
	
	private WritableDataBufImpl<TYPE> builder0(TYPE t, int capacity) {
		WritableDataBufImpl<TYPE> buf = DataBuffers.newWritable0(capacity);
		buf.factory = this;
		buf.type = t;
		return buf;
	}
	
	// PacketFactoryInternal
	
	@Override
	public SimplePacket make(WritableDataBufImpl<TYPE> buf) {
		buf.seek(0);
		return new Packet250Fake<TYPE>(buf, this, buf.type);
	}

	@Override
	public <T> PacketBuilder builderWithResponseHandler(TYPE type, int capacity, ModPacket.WithResponse<T> packet, PacketResponseHandler<? super T> handler) {
		return null;
	}

	@Override
	public PacketBuilder response() {
		PacketBuilder builder = SCModContainer.packets.builder(SCPackets.RESPONSE);
		// TODO
		return builder;
	}

}
