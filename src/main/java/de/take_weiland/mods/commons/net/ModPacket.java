package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.PacketWithFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every PacketType.
 * Register your Types with {@link de.take_weiland.mods.commons.net.Network#simplePacketHandler(String, Class)}</p>
 * <p>To send this packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket}. Example:
 * <pre>{@code
 *new ExamplePacket(someData, "moreData").sendToServer();
 *new DifferentPacket(evenMoreData).sendToPlayer(somePlayer);
 * }</pre></p>
 */
public abstract class ModPacket<TYPE extends Enum<TYPE> & SimplePacketType<TYPE>> implements SimplePacket {

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
	 * @return
	 */
	protected int expectedSize() {
		return 32;
	}
	
	private SimplePacket delegate;
	private SimplePacket make() {
		SimplePacket delegate;
		if ((delegate = this.delegate) == null) {
			delegate = this.delegate = make0();
		}
		return delegate;
	}
	
	private SimplePacket make0() {
		@SuppressWarnings("unchecked") // safe, ASM generated
		PacketWithFactory<TYPE> pwf = (PacketWithFactory<TYPE>) this;
		
		PacketBuilder builder = pwf._sc_getFactory().builder(pwf._sc_getType(), expectedSize());
		write(builder);
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public final TYPE type() {
		return ((PacketWithFactory<TYPE>) this)._sc_getType();
	}

	@Override
	public final void sendTo(PacketTarget target) {
		make().sendTo(target);
	}

	@Override
	public final void sendToServer() {
		make().sendToServer();
	}

	@Override
	public final void sendTo(EntityPlayer player) {
		make().sendTo(player);
	}

	@Override
	public final void sendTo(Iterable<? extends EntityPlayer> players) {
		make().sendTo(players);
	}

	@Override
	public final void sendToAll() {
		make().sendToAll();
	}

	@Override
	public final void sendToAllInDimension(int dimension) {
		make().sendToAllInDimension(dimension);
	}

	@Override
	public final void sendToAllInDimension(World world) {
		make().sendToAllInDimension(world);
	}

	@Override
	public final void sendToAllNear(World world, double x, double y, double z, double radius) {
		make().sendToAllNear(world, x, y, z, radius);
	}

	@Override
	public final void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		make().sendToAllNear(dimension, x, y, z, radius);
	}

	@Override
	public final void sendToAllNear(Entity entity, double radius) {
		make().sendToAllNear(entity, radius);
	}

	@Override
	public final void sendToAllNear(TileEntity te, double radius) {
		make().sendToAllNear(te, radius);
	}

	@Override
	public final void sendToAllTracking(Entity entity) {
		make().sendToAllTracking(entity);
	}

	@Override
	public final void sendToAllTracking(TileEntity te) {
		make().sendToAllTracking(te);
	}

	@Override
	public final void sendToAllAssociated(Entity e) {
		make().sendToAllAssociated(e);
	}

	@Override
	public final void sendToViewing(Container c) {
		make().sendToViewing(c);
	}
}
