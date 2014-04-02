package de.take_weiland.mods.commons.net;

import com.google.common.collect.Maps;
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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.google.common.base.Preconditions.*;
import static de.take_weiland.mods.commons.net.Network.logger;

final class FMLPacketHandlerImpl<TYPE extends Enum<TYPE>> implements IPacketHandler, PacketFactory<TYPE>, PacketFactoryInternal<TYPE> {

	private static final int MAX_PACKETS = (Integer.MAX_VALUE >> 1) - 1;

	final String channel;
	final PacketHandler<TYPE> handler;
	private final Class<TYPE> typeClass;
	final int idSize;
	final int expectsResponseFlag;
	final int responseId;

	private ConcurrentMap<Integer, PacketResponseHandler> responseHandlers;
	private AtomicInteger nextTransferId;

	FMLPacketHandlerImpl(String channel, PacketHandler<TYPE> handler, Class<TYPE> typeClass) {
		this.channel = checkNotNull(channel);
		this.handler = checkNotNull(handler);
		this.typeClass = checkNotNull(typeClass);
		int len = JavaUtils.getEnumConstantsShared(typeClass).length;
		checkArgument(len > 0, "Must have at least one packet type!");
		checkArgument(len < MAX_PACKETS, "Too many packets, can handle at most %d", MAX_PACKETS);

		this.responseId = len; // use next available ID as the createResponse packet
		int highestOneBit = Integer.highestOneBit(len); // "len" is a valid id, too
		this.expectsResponseFlag = highestOneBit << 1;

		// 1st "+1" because the highestOneBit itself counts, too
		// 2nd "+1" for the expectsResponse-flag
		int bitsUsed = Integer.numberOfTrailingZeros(highestOneBit) + 1 + 1;
		this.idSize = (bitsUsed >> 3) + 1;

		NetworkRegistry.instance().registerChannel(this, channel);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(String.format("Setup channel %s with %d packets and IdSize %d.", channel, len, idSize));
		}
	}

	ConcurrentMap<Integer, PacketResponseHandler> responseHandlers() {
		ConcurrentMap<Integer, PacketResponseHandler> map = responseHandlers;
		if (map == null) {
			synchronized (this) {
				if ((map = responseHandlers) == null) {
					map = responseHandlers = Maps.newConcurrentMap();
				}
			}
		}
		return map;
	}

	int nextTransferId() {
		AtomicInteger i = nextTransferId;
		if (i == null) {
			synchronized (this) {
				if ((i = nextTransferId) == null) {
					i = nextTransferId = new AtomicInteger(0);
				}
			}
		}
		return i.getAndIncrement();
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
		if (id == responseId) {
			// ResponseHandler is set when packets are send via MemoryConnection
			// see explanatory comment on createResponse
			if (buf.responseHandler != null) {
				buf.responseHandler.onResponse(buf, player);
			} else {
				int transferId = buf.getInt();
				PacketResponseHandler responseHandler;
				if (responseHandlers == null || ((responseHandler = responseHandlers.remove(transferId))) == null) {
					logger.warning(String.format("Received unknown transferId %d from %s", transferId, player.username));
				} else {
					responseHandler.onResponse(buf, player);
				}
			}
		} else {
			boolean expectsResponse = (id & expectsResponseFlag) != 0;
			if (expectsResponse) {
				if (buf.responseHandler == null) {
					buf.transferId = buf.getInt();
				}
			}
			Side side = Sides.logical(player);
			buf.sender = side.isClient() ? null : player;
			handler.handle(JavaUtils.byOrdinal(typeClass, (id & ~expectsResponseFlag)), buf, player, side);
		}
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
	public PacketBuilder.ForResponse createResponse(int capacity, PacketBufferImpl<TYPE> input) {
		checkState((input.id & expectsResponseFlag) != 0, "Packet didn't expect a response!");
		PacketBufferImpl<TYPE> response = new PacketBufferImpl<>(capacity, this, responseId);
		// Copy the ResponseHandler over for some optimization on MemoryConnection
		// Example: Integrated server creates new PacketBuilder, sets ResponseHandler and sends to client
		// Client receives and reads from same PacketBuffer (which still has ResponseHandler set)
		// then .response() is called on client which calls this method.
		// so the input still has the original responseHandler set
		// handle0 will check for responseHandler on a response packet
		response.responseHandler = input.responseHandler;
		response.sender = input.sender;
		response.transferId = input.transferId;
		return response;
	}

	@Override
	public void onResponseHandlerSet(PacketBufferImpl<TYPE> buffer, PacketResponseHandler handler) {
		buffer.id |= expectsResponseFlag;
	}
}
