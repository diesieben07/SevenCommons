package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import de.take_weiland.mods.commons.internal.SimplePacketTypeProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public abstract class ModPacketBase implements SimplePacket {

	/**
	 * Writes this packet's data to the given buffer
	 * @param buffer a buffer for this packet's data
	 */
	protected abstract void write(WritableDataBuf buffer);

	/**
	 * whether the given (logical) side can receive this packet
	 * @param side the receiving side to check
	 * @return true whether Packet is valid to be received on the given side
	 */
	protected abstract boolean validOn(Side side);

	/**
	 * Determine an expected size for this packet to accurately size the {@link de.take_weiland.mods.commons.net.WritableDataBuf} passed to {@link #write(WritableDataBuf)}
	 * @return an expected size in bytes
	 */
	protected int expectedSize() {
		return 32;
	}

	abstract void handle0(DataBuf buffer, EntityPlayer player, Side side);

	abstract void write0(WritableDataBuf buf);

	SimplePacket postWrite(SimplePacket packet, PacketFactoryInternal<?> factory) {
		return packet;
	}

	@SuppressWarnings("unchecked") // safe, ASM generated code
	private <TYPE extends Enum<TYPE> & SimplePacketType & SimplePacketTypeProxy> SimplePacket make0() {
		ModPacketProxy<TYPE> proxy = (ModPacketProxy<TYPE>) this;
		TYPE type = proxy._sc$getPacketType();

		PacketFactoryInternal<TYPE> factory = (PacketFactoryInternal<TYPE>) type._sc$getPacketFactory();
		PacketBuilder builder = factory.builder(type, expectedSize());
		write0(builder);
		return postWrite(builder.build(), factory);
	}

	private SimplePacket delegate;

	private SimplePacket make() {
		SimplePacket s;
		return (s = delegate) == null ? (delegate = make0()) : s;
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
