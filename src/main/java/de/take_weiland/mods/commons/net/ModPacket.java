package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.ResponsePacket;
import de.take_weiland.mods.commons.internal.SCPackets;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every PacketType.
 * Register your Types with {@link Network#simplePacketHandler(String, Class)}</p>
 * <p>To send this packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket}. Example:
 * <pre>{@code
 *new ExamplePacket(someData, "moreData").sendToServer();
 *new DifferentPacket(evenMoreData).sendToPlayer(somePlayer);
 * }</pre></p>
 */
public abstract class ModPacket {

	/**
	 * whether the given (logical) side can receive this packet
	 * @param side the receiving side to check
	 * @return true whether Packet is valid to be received on the given side
	 */
	protected abstract boolean validOn(Side side);

	/**
	 * Writes this packet's data to the given buffer
	 * @param buffer a buffer for this packet's data
	 */
	protected abstract void write(WritableDataBuf buffer);

	/**
	 * Reads this packet's data from the given buffer (as written by {@link #write(WritableDataBuf)} and then performs this packet's action.
	 * @param buffer the buffer containing the packet data
	 * @param player the player handling this packet, on the client side it's the client-player, on the server side it's the player sending the packet
	 * @param side the (logical) side receiving the packet
	 */
	protected abstract void handle(DataBuf buffer, EntityPlayer player, Side side);

	/**
	 * Determine an expected size for this packet to accurately size the {@link de.take_weiland.mods.commons.net.WritableDataBuf} passed to {@link #write(WritableDataBuf)}
	 * @return an expected size in bytes
	 */
	protected int expectedSize() {
		return 32;
	}

	public static abstract class WithResponse<T> extends ModPacket implements SimplePacket.WithResponse<T> {

		protected abstract void handle(DataBuf in, EntityPlayer player, Side side, WritableDataBuf out);

		public abstract T readResponse(DataBuf in, EntityPlayer player, Side side);

		@Override
		void write0(WritableDataBuf buf) {
			buf.putInt(transferId);
			write(buf);
		}

		@Override
		protected final void handle(DataBuf buffer, EntityPlayer player, Side side) {
			int transferId = buffer.getInt();
			PacketBuilder response = SCModContainer.packetFactory.builder(SCPackets.RESPONSE);
			response.putInt(transferId);
			handle(buffer, player, side, response);
			if (side.isClient()) {
				response.build().sendToServer();
			} else {
				System.out.println("Handling and sending to player: " + player);
				response.build().sendTo(player);
			}
		}

		private SimplePacket clearMake() {
			delegate = null;
			transferId = ResponsePacket.nextTransferId();
			inResponseMode = true;
			return make();
		}

		private int transferId;
		private boolean inResponseMode = false;

		@SuppressWarnings("unchecked") // cast is safe, see ModPacketWithResponseTransformer
		private SimplePacket.ResponseSentToServer<T> castMe() {
			return (SimplePacket.ResponseSentToServer<T>) this;
		}

		// interface SimplePacket.ResponseSentToServer is added by ASM
		@SuppressWarnings("unchecked") // cast is safe, ASM generated code
		SimplePacket.WithResponse<T> onResponse(ClientResponseHandler<? super T> handler) {
			return onResponse((PacketResponseHandler<? super T>) handler);
		}

		SimplePacket.WithResponse<T> onResponse(PacketResponseHandler<? super T> handler) {
			checkState(inResponseMode, "incorrect usage of SimplePacket.WithResponse fluent interface!");
			checkNotNull(handler, "handler");
			// now thats what I call type casting!
			ResponsePacket.registerHandler(transferId, this, handler);
			inResponseMode = false;
			return this;
		}

		SimplePacket.WithResponse<T> discardResponse() {
			inResponseMode = false;
			return this;
		}

		@Override
		public SimplePacket.ResponseSent<T> sendTo(PacketTarget target) {
			clearMake().sendTo(target);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSentToServer<T> sendToServer() {
			clearMake().sendToServer();
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendTo(EntityPlayer player) {
			clearMake().sendTo(player);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendTo(Iterable<? extends EntityPlayer> players) {
			clearMake().sendTo(players);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAll() {
			clearMake().sendToAll();
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllInDimension(int dimension) {
			clearMake().sendToAllInDimension(dimension);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllInDimension(World world) {
			clearMake().sendToAllInDimension(world);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllNear(World world, double x, double y, double z, double radius) {
			clearMake().sendToAllNear(world, x, y, z, radius);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllNear(int dimension, double x, double y, double z, double radius) {
			clearMake().sendToAllNear(dimension, x, y, z, radius);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllNear(Entity entity, double radius) {
			clearMake().sendToAllNear(entity, radius);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllNear(TileEntity te, double radius) {
			clearMake().sendToAllNear(te, radius);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllTracking(Entity entity) {
			clearMake().sendToAllTracking(entity);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllTracking(TileEntity te) {
			clearMake().sendToAllTracking(te);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToAllAssociated(Entity e) {
			clearMake().sendToAllAssociated(e);
			return castMe();
		}

		@Override
		public SimplePacket.ResponseSent<T> sendToViewing(Container c) {
			clearMake().sendToViewing(c);
			return castMe();
		}
	}

	void write0(WritableDataBuf buf) {
		write(buf);
	}

	SimplePacket delegate;
	SimplePacket make() {
		SimplePacket delegate;
		if ((delegate = this.delegate) == null) {
			delegate = this.delegate = make0();
		}
		return delegate;
	}

	@SuppressWarnings("unchecked") // safe, ASM generated code
	private <TYPE extends Enum<TYPE> & SimplePacketType & SimplePacketTypeProxy> SimplePacket make0() {
		ModPacketProxy<TYPE> proxy = (ModPacketProxy<TYPE>) this;
		TYPE type = proxy._sc$getPacketType();

		PacketBuilder builder = ((PacketFactory<TYPE>)type._sc$getPacketFactory()).builder(type, expectedSize());
		write0(builder);
		return builder.build();
	}

	public SimplePacket sendTo(PacketTarget target) {
		return make().sendTo(target);
	}

	public SimplePacket sendToServer() {
		return make().sendToServer();
	}

	public SimplePacket sendTo(EntityPlayer player) {
		return make().sendTo(player);
	}

	public SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
		return make().sendTo(players);
	}

	public SimplePacket sendToAll() {
		return make().sendToAll();
	}

	public SimplePacket sendToAllInDimension(int dimension) {
		return make().sendToAllInDimension(dimension);
	}

	public SimplePacket sendToAllInDimension(World world) {
		return make().sendToAllInDimension(world);
	}

	public SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
		return make().sendToAllNear(world, x, y, z, radius);
	}

	public SimplePacket sendToAllNear(int dimension, double x, double y, double z, double radius) {
		return make().sendToAllNear(dimension, x, y, z, radius);
	}

	public SimplePacket sendToAllNear(Entity entity, double radius) {
		return make().sendToAllNear(entity, radius);
	}

	public SimplePacket sendToAllNear(TileEntity te, double radius) {
		return make().sendToAllNear(te, radius);
	}

	public SimplePacket sendToAllTracking(Entity entity) {
		return make().sendToAllTracking(entity);
	}

	public SimplePacket sendToAllTracking(TileEntity te) {
		return make().sendToAllTracking(te);
	}

	public SimplePacket sendToAllAssociated(Entity e) {
		return make().sendToAllAssociated(e);
	}

	public SimplePacket sendToViewing(Container c) {
		return make().sendToViewing(c);
	}
}
