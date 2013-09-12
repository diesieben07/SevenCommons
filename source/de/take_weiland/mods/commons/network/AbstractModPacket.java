package de.take_weiland.mods.commons.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.CollectionUtils;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Sides;

/**
 * Root class for all your mod packets<br>
 * Will get wrapped into a vanilla packet for sending
 * @author diesieben07
 *
 */
public abstract class AbstractModPacket implements ModPacket {

	private Packet packet;

	protected abstract void readData(byte[] data);
	
	protected abstract byte[] writeData();
	
	/**
	 * performs this packet's action
	 * @param player the player appropriate for this packet (the sending player on the server, the client player on the client)
	 * @param side the current logical side
	 */
	protected abstract void execute(EntityPlayer player, Side side);

	/**
	 * get the {@link PacketType} associated with this packet
	 * @return
	 */
	protected abstract PacketType getType();

	/**
	 * determines if the given logical side can receive this packet
	 * @param side the logical side
	 * @return true if it is valid for the given logical side to receive this packet
	 */
	protected boolean isValidForSide(Side side) {
		return true;
	}

	/**
	 * generate a new Vanilla {@link Packet} that represents this packet
	 * @return a packet
	 */
	public final Packet getVanillaPacket() {
		if (packet == null) {
			packet = PacketDispatcher.getPacket(getType().getChannel(), writeData());
		}
		return packet;
	}
	
	@Override
	public final void sendToServer() {
		PacketDispatcher.sendPacketToServer(getVanillaPacket());
	}

	@Override
	public final void sendTo(EntityPlayer player) {
		PacketDispatcher.sendPacketToPlayer(getVanillaPacket(), (Player)player);
	}
	
	@Override
	public void sendTo(EntityPlayer... players) {
		Packet packet = getVanillaPacket();
		for (EntityPlayer player : players) {
			PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
		}
	}
	
	@Override
	public final void sendTo(Iterable<? extends EntityPlayer> players) {
		Packet packet = getVanillaPacket();
		for (EntityPlayer player : players) {
			PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
		}
	}

	@Override
	public final void sendToAll() {
		PacketDispatcher.sendPacketToAllPlayers(getVanillaPacket());
	}

	@Override
	public final void sendToAllInDimension(int dimension) {
		PacketDispatcher.sendPacketToAllInDimension(getVanillaPacket(), dimension);
	}

	@Override
	public final void sendToAllInDimension(World world) {
		PacketDispatcher.sendPacketToAllInDimension(getVanillaPacket(), world.provider.dimensionId);
	}

	@Override
	public final void sendToAllNear(World world, double x, double y, double z, double radius) {
		sendToAllNear(world.provider.dimensionId, x, y, z, radius);
	}

	@Override
	public final void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(x, y, z, radius, dimension, getVanillaPacket());
	}

	@Override
	public final void sendToAllNear(Entity entity, double radius) {
		sendToAllNear(entity.worldObj.provider.dimensionId, entity.posX, entity.posY, entity.posZ, radius);
	}

	@Override
	public final void sendToAllNear(TileEntity tileEntity, double radius) {
		sendToAllNear(tileEntity.worldObj.provider.dimensionId, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, radius);
	}

	@Override
	public final void sendToAllTracking(Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, getVanillaPacket());
		}
	}

	@Override
	public final void sendToOps() {
		sendTo(Players.getOps());
	}
	
	public static final <E extends Enum<E>> E readEnum(Class<E> clazz, ByteArrayDataInput in) {
		return CollectionUtils.safeArrayAccess(clazz.getEnumConstants(), in.readUnsignedByte());
	}

	public static final void writeEnum(Enum<?> element, ByteArrayDataOutput out) {
		out.writeByte(UnsignedBytes.checkedCast(element.ordinal()));
	}

}
