package de.take_weiland.mods.commons.network;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.ModdingUtils;

/**
 * Root class for all your mod packets<br>
 * Will get wrapped into a vanilla packet for sending
 * @author diesieben07
 *
 */
public abstract class ModPacket {

	/**
	 * reads this packet's data from the stream
	 * @param in
	 */
	protected abstract void readData(ByteArrayDataInput in);
	
	/**
	 * writes this packet's data to the stream
	 * @param out
	 */
	protected abstract void writeData(ByteArrayDataOutput out);
	
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
	 * determines if the given side can receive this packet
	 * @param side
	 * @return true if it is valid for the given side to receive this packet
	 */
	protected boolean isValidForSide(Side side) {
		return true;
	}
	
	public final Packet getVanillaPacket() {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		
		out.writeByte(getType().getPacketId());
		writeData(out);
		return PacketDispatcher.getPacket(getType().getChannel(), out.toByteArray());
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
		if (ModdingUtils.determineSide(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, getVanillaPacket());
		}
	}
	
	public final void sendToOps() {
		Set<String> ops = ModdingUtils.getOpsRaw();
		for (EntityPlayer player : ModdingUtils.getAllPlayers()) {
			if (ops.contains(player.username.toLowerCase().trim())) {
				PacketDispatcher.sendPacketToPlayer(getVanillaPacket(), (Player)player);
			}
		}
	}
}
