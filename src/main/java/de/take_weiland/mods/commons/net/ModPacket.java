package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.SCPackets;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static com.google.common.base.Preconditions.checkNotNull;

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
			PacketBuilder response = SCModContainer.packets.builder(SCPackets.RESPONSE);
			response.putInt(transferId);
			handle(buffer, player, side, response);
			if (side.isClient()) {
				response.build().sendToServer();
			} else {
				System.out.println("Handling and sending to player: " + player);
				response.build().sendTo(player);
			}
		}

		@Override
		<TYPE extends Enum<TYPE> & SimplePacketType & SimplePacketTypeProxy> PacketBuilder getPacketBuilder(TYPE type, PacketFactoryInternal<TYPE> factory) {
			if (currentHandler != null) {
				return factory.builderWithResponseHandler(type, expectedSize(), this, currentHandler);
			} else {
				return factory.builder(type, expectedSize());
			}
		}

		private PacketResponseHandler<? super T> currentHandler;
		private int transferId;

		@Override
		public SimplePacket.WithResponse<T> onResponse(PacketResponseHandler<? super T> handler) {
			currentHandler = checkNotNull(handler);
			delegate = null;
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> discardResponse() {
			currentHandler = null;
			delegate = null;
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToViewing(Container c) {
			super.sendToViewing(c);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendTo(PacketTarget target) {
			super.sendTo(target);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToServer() {
			super.sendToServer();
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendTo(EntityPlayer player) {
			super.sendTo(player);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendTo(Iterable<? extends EntityPlayer> players) {
			super.sendTo(players);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAll() {
			super.sendToAll();
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllInDimension(int dimension) {
			super.sendToAllInDimension(dimension);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllInDimension(World world) {
			super.sendToAllInDimension(world);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllNear(World world, double x, double y, double z, double radius) {
			super.sendToAllNear(world, x, y, z, radius);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllNear(int dimension, double x, double y, double z, double radius) {
			super.sendToAllNear(dimension, x, y, z, radius);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllNear(Entity entity, double radius) {
			super.sendToAllNear(entity, radius);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllNear(TileEntity te, double radius) {
			super.sendToAllNear(te, radius);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllTracking(Entity entity) {
			super.sendToAllTracking(entity);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllTracking(TileEntity te) {
			super.sendToAllTracking(te);
			return this;
		}

		@Override
		public SimplePacket.WithResponse<T> sendToAllAssociated(Entity e) {
			super.sendToAllAssociated(e);
			return this;
		}
	}

	void write0(WritableDataBuf buf) {
		write(buf);
	}

	SimplePacket delegate;
	final SimplePacket make() {
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

		PacketBuilder builder = getPacketBuilder(type, (PacketFactoryInternal<TYPE>)type._sc$getPacketFactory());
		write0(builder);
		return builder.build();
	}

	<TYPE extends Enum<TYPE> & SimplePacketType & SimplePacketTypeProxy> PacketBuilder getPacketBuilder(TYPE type, PacketFactoryInternal<TYPE> factory) {
		return factory.builder(type, expectedSize());
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
