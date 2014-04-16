package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import java.io.DataOutput;
import java.io.IOException;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static de.take_weiland.mods.commons.net.Network.logger;

final class FMLPacketHandlerImpl<TYPE extends Enum<TYPE>> implements IPacketHandler, PacketFactory<TYPE>, PacketFactoryInternal<TYPE> {

	private static final int MAX_PACKETS = Integer.MAX_VALUE;

	final String channel;
	final PacketHandler<TYPE> handler;
	private final Class<TYPE> typeClass;
	final int idSize;

	FMLPacketHandlerImpl(String channel, PacketHandler<TYPE> handler, Class<TYPE> typeClass) {
		this.channel = checkNotNull(channel);
		this.handler = checkNotNull(handler);
		this.typeClass = checkNotNull(typeClass);
		int len = JavaUtils.getEnumConstantsShared(typeClass).length;
		checkArgument(len > 0, "Must have at least one packet type!");
		checkArgument(len < MAX_PACKETS, "Too many packets, can handle at most %d", MAX_PACKETS);

		int highestOneBit = Integer.highestOneBit(len - 1) << 1;
		int bitsUsed = Integer.numberOfTrailingZeros(highestOneBit);
		this.idSize = (bitsUsed >> 3) + 1;

		NetworkRegistry.instance().registerChannel(this, channel);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format("Setup channel %s with %d packets and IdSize %d.", channel, len, idSize));
		}
	}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player fmlPlayer) {
		byte[] buf = packet.data;
		EntityPlayer player = (EntityPlayer) fmlPlayer;

		int id = readId(buf);

		PacketBufferImpl<TYPE> dataBuf = new PacketBufferImpl<>(buf, this, id);
		dataBuf.seek(idSize);
		handle0(dataBuf, id, player);
	}

	private int readId(byte[] buf) {
		int result = 0;
		for (int i = 0; i < idSize; ++i) {
			result |= buf[i] << (i << 3);
		}
		return result;
	}


	void writePacketId(DataOutput out, int id) throws IOException {
		for (int i = 0; i < idSize; ++i) {
			int shift = i << 3;
			out.writeByte((id & (0xFF << shift)) >> shift);
		}
	}

	void handle0(PacketBufferImpl<TYPE> buf, int id, EntityPlayer player) {
		Side side = Sides.logical(player);
		buf.sender = side.isClient() ? null : player;
		handler.handle(JavaUtils.byOrdinal(typeClass, id), buf, player, side);
	}

	@Override
	public PacketBuilder builder(TYPE t) {
		return builder0(t.ordinal(), -1); // -1 will pick the default capacity
	}
	
	@Override
	public PacketBuilder builder(TYPE t, int capacity) {
		checkArgument(capacity > 0, "capacity must be > 0");
		return builder0(t.ordinal(), capacity);
	}
	
	private PacketBufferImpl<TYPE> builder0(int id, int capacity) {
		return new PacketBufferImpl<>(capacity, this, id);
	}
	
	// PacketFactoryInternal
	
	@Override
	public SimplePacket build(PacketBufferImpl<TYPE> buf) {
		buf.seek(0);
		return new Packet250Fake<>(buf, this, buf.id);
	}

	@Override
	public void registerCallback(SimplePacket wrapper, ModPacket.WithResponse<?> packet) {
		((Packet250Fake<?>) wrapper).writeCallback = packet;
	}
}
