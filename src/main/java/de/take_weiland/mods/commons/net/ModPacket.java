package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.ModPacketWithResponseProxy;
import de.take_weiland.mods.commons.internal.SCPackets;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every PacketType.
 * Register your Types with {@link Network#simplePacketHandler(String, Class)}</p>
 * <p>To send this packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket}. Example:
 * <pre>{@code
 *new ExamplePacket(someData, "moreData").sendToServer();
 *new DifferentPacket(evenMoreData).sendToPlayer(somePlayer);
 * }</pre></p>
 */
public abstract class ModPacket implements SimplePacket {

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

	public static abstract class WithResponse<T> extends ModPacket {

		protected abstract void handle(DataBuf in, EntityPlayer player, Side side, WritableDataBuf out);

		public abstract T readResponse(DataBuf in, EntityPlayer player, Side side);

		@Override
		void write0(WritableDataBuf buf) {
			int transferId = ((ModPacketWithResponseProxy) this)._sc$nextTransferId();
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
				response.build().sendTo(player);
			}
		}

	}

	void write0(WritableDataBuf buf) {
		write(buf);
	}
	
	private SimplePacket delegate;
	private SimplePacket make() {
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

	@Override
	public final SimplePacket sendTo(PacketTarget target) {
		return make().sendTo(target);
	}

	@Override
	public final SimplePacket sendToServer() {
		return make().sendToServer();
	}

	@Override
	public final SimplePacket sendTo(EntityPlayer player) {
		return make().sendTo(player);
	}

	@Override
	public final SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
		return make().sendTo(players);
	}

	@Override
	public final SimplePacket sendToAll() {
		return make().sendToAll();
	}

	@Override
	public final SimplePacket sendToAllInDimension(int dimension) {
		return make().sendToAllInDimension(dimension);
	}

	@Override
	public final SimplePacket sendToAllInDimension(World world) {
		return make().sendToAllInDimension(world);
	}

	@Override
	public final SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
		return make().sendToAllNear(world, x, y, z, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(int dimension, double x, double y, double z, double radius) {
		return make().sendToAllNear(dimension, x, y, z, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(Entity entity, double radius) {
		return make().sendToAllNear(entity, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(TileEntity te, double radius) {
		return make().sendToAllNear(te, radius);
	}

	@Override
	public final SimplePacket sendToAllTracking(Entity entity) {
		return make().sendToAllTracking(entity);
	}

	@Override
	public final SimplePacket sendToAllTracking(TileEntity te) {
		return make().sendToAllTracking(te);
	}

	@Override
	public final SimplePacket sendToAllAssociated(Entity e) {
		return make().sendToAllAssociated(e);
	}

	@Override
	public final SimplePacket sendToViewing(Container c) {
		return make().sendToViewing(c);
	}
}
