package de.take_weiland.mods.commons.net;

import com.google.common.collect.MapMaker;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every PacketType.
 * Register your Types with {@link Network#simplePacketHandler(String, Class)}</p>
 * <p>To send this packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket}. Example:
 * <pre>{@code
 * new ExamplePacket(someData, "moreData").sendToServer();
 * new DifferentPacket(evenMoreData).sendToPlayer(somePlayer);
 * }</pre></p>
 */
public abstract class ModPacket extends ModPacketBase {

	/**
	 * Reads this packet's data from the given buffer (as written by {@link #write(WritableDataBuf)} and then performs this packet's action.
	 *
	 * @param buffer the buffer containing the packet data
	 * @param player the player handling this packet, on the client side it's the client-player, on the server side it's the player sending the packet
	 * @param side   the (logical) side receiving the packet
	 */
	protected abstract void handle(DataBuf buffer, EntityPlayer player, Side side);

	@Override
	void write0(WritableDataBuf buf) {
		write(buf);
	}

	@Override
	void handle0(DataBuf buffer, EntityPlayer player, Side side) {
		handle(buffer, player, side);
	}

	public static abstract class WithResponse<T extends Response> extends ModPacketBase {

		public final ModPacket.WithResponse<T> waitAtMost(long delay, TimeUnit unit) {
			maxWaitMillis = Math.max(maxWaitMillis, unit.toMillis(delay));
			return this;
		}

		public final WithResponse<T> onResponse(PacketResponseHandler<? super T> handler) {
			checkState(this.handler == null, "Cannot register multiple handlers!");
			this.handler = checkNotNull(handler, "handler");
			return this;
		}

		protected abstract T handle(DataBuf in, EntityPlayer player, Side side);

		protected abstract Class<T> responseClass();

		protected WithResponse() {
			checkNotNull(responseClass(), "ResponseClass must not be null!");
		}

		private static final AtomicInteger nextTransferId = new AtomicInteger();
		private final AtomicInteger pendingResponses = new AtomicInteger();
		private int transferId;

		void preSend() {
			pendingResponses.incrementAndGet();
		}

		@Override
		SimplePacket postWrite(SimplePacket packet, PacketFactoryInternal<?> factory) {
			factory.registerCallback(packet, this);
			return packet;
		}

		private static int nextTransferId() {
			int id;
			do {
				id = nextTransferId.getAndIncrement();
			} while (id == -1);
			return id;
		}

		@Override
		void write0(WritableDataBuf buf) {
			transferId = nextTransferId();
			register(transferId, maxWaitMillis < 0 ? DEFAULT_WAIT_MILLIS : maxWaitMillis, this);

			buf.writeInt(transferId);
			write(buf);
		}

		@Override
		void handle0(DataBuf buffer, EntityPlayer player, Side side) {
			int transferId = buffer.readInt();
			if (side.isClient()) {
				handle(buffer, player, side).setResponseTransferId(transferId).sendToServer();
			} else {
				handle(buffer, player, side).setResponseTransferId(transferId).sendTo(player);
			}
		}

		private static final ConcurrentMap<Integer, WithResponse<?>> waiters = new MapMaker().concurrencyLevel(2).makeMap();

		static void register(final int transferId, long maxWaitMillis, WithResponse<?> packet) {
			if (waiters.put(transferId, packet) != null) {
				throw new IllegalStateException("Duplicate TransferID!");
			}
			Scheduler.forEnvironment().scheduleSimple(new Runnable() {
				@Override
				public void run() {
					waiters.remove(transferId);
				}
			}, maxWaitMillis, TimeUnit.MILLISECONDS);
		}

		static <T extends Response> void onResponse(Integer transferId, T response, EntityPlayer player) {
			@SuppressWarnings("unchecked")
			WithResponse<? super T> waiter = (WithResponse<? super T>) waiters.get(transferId);
			if (waiter != null && waiter.fireResponse(response, player)) {
				waiters.remove(transferId);
			}
		}

		private static final long DEFAULT_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30);
		private PacketResponseHandler<? super T> handler;
		private long maxWaitMillis = -1;

		boolean fireResponse(T packet, EntityPlayer responder) {
			PacketResponseHandler<? super T> handler;
			if ((handler = this.handler) != null) {
				handler.onResponse(packet, responder);
			}
			return pendingResponses.decrementAndGet() <= 0;
		}
	}

	public static abstract class Response extends ModPacket {

		private int responseTransferId = -1;

		Response setResponseTransferId(int id) {
			responseTransferId = id;
			return this;
		}

		@Override
		void write0(WritableDataBuf buf) {
			buf.writeInt(responseTransferId);
			super.write0(buf);
		}

		@Override
		void handle0(DataBuf buffer, EntityPlayer player, Side side) {
			int transId = buffer.readInt();
			super.handle0(buffer, player, side);
			WithResponse.onResponse(transId, this, player);
		}
	}

}
