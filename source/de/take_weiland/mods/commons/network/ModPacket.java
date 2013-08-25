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
import de.take_weiland.mods.commons.util.CommonUtils;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Sides;

/**
 * Root class for all your mod packets<br>
 * Will get wrapped into a vanilla packet for sending
 * @author diesieben07
 *
 */
public abstract class ModPacket {

	private Packet packet;

	protected abstract void readData(byte[] data);
	
	protected abstract byte[] writeData(byte packetId);
	
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
			packet = PacketDispatcher.getPacket(getType().getChannel(), writeData(getType().getPacketId()));
		}
		return packet;
	}
	
	public final void sendToServer() {
		PacketDispatcher.sendPacketToServer(getVanillaPacket());
	}

	public final void sendToPlayer(EntityPlayer player) {
		PacketDispatcher.sendPacketToPlayer(getVanillaPacket(), (Player)player);
	}

	public final void sendToAll() {
		PacketDispatcher.sendPacketToAllPlayers(getVanillaPacket());
	}

	public final void sendToAllInDimension(int dimension) {
		PacketDispatcher.sendPacketToAllInDimension(getVanillaPacket(), dimension);
	}

	public final void sendToAllInDimension(World world) {
		PacketDispatcher.sendPacketToAllInDimension(getVanillaPacket(), world.provider.dimensionId);
	}

	public final void sendToAllNear(World world, double x, double y, double z, double radius) {
		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(x, y, z, radius, world.provider.dimensionId, getVanillaPacket());
	}

	public final void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(x, y, z, radius, dimension, getVanillaPacket());
	}

	public final void sendToAllNear(Entity entity, double radius) {
		sendToAllNear(entity.worldObj, entity.posX, entity.posY, entity.posZ, radius);
	}

	public final void sendToAllNear(TileEntity tileEntity, double radius) {
		sendToAllNear(tileEntity.worldObj, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, radius);
	}

	public final void sendToAllTracking(Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, getVanillaPacket());
		}
	}

	public final void sendToOps() {
		Packet packet = getVanillaPacket();
		for (EntityPlayer op : Players.getOps()) {
			PacketDispatcher.sendPacketToPlayer(packet, (Player)op);
		}
	}

	public static final <E extends Enum<E>> E readEnum(Class<E> clazz, ByteArrayDataInput in) {
		return CommonUtils.safeArrayAccess(clazz.getEnumConstants(), in.readUnsignedByte());
	}

	public static final void writeEnum(Enum<?> element, ByteArrayDataOutput out) {
		out.writeByte(UnsignedBytes.checkedCast(element.ordinal()));
	}

}
