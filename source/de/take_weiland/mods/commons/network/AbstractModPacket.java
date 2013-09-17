package de.take_weiland.mods.commons.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.util.Sides;

public abstract class AbstractModPacket implements ModPacket {

	private Packet vanillaPacket;
	
	public final Packet make() {
		return vanillaPacket != null ? vanillaPacket : (vanillaPacket = type().transport().toVanilla(this));
	}

	@Override
	public final void sendToServer() {
		PacketDispatcher.sendPacketToServer(make());
	}

	@Override
	public void sendTo(EntityPlayer player) {
		PacketDispatcher.sendPacketToPlayer(make(), (Player)player);
	}

	@Override
	public void sendTo(EntityPlayer... players) {
		for (EntityPlayer player : players) {
			sendTo(player);
		}
	}

	@Override
	public void sendTo(Iterable<? extends EntityPlayer> players) {
		for (EntityPlayer player : players) {
			sendTo(player);
		}
	}
	
	@Override
	public void sendToAll() {
		PacketDispatcher.sendPacketToAllPlayers(make());
	}

	@Override
	public void sendToAllInDimension(int dimension) {
		PacketDispatcher.sendPacketToAllInDimension(make(), dimension);
	}

	@Override
	public void sendToAllInDimension(World world) {
		PacketDispatcher.sendPacketToAllInDimension(make(), world.provider.dimensionId);
	}

	@Override
	public void sendToAllNear(World world, double x, double y, double z, double radius) {
		PacketDispatcher.sendPacketToAllAround(y, y, z, radius, world.provider.dimensionId, make());
	}

	@Override
	public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		PacketDispatcher.sendPacketToAllAround(y, y, z, radius, dimension, make());
	}

	@Override
	public void sendToAllNear(Entity entity, double radius) {
		PacketDispatcher.sendPacketToAllAround(entity.posX, entity.posY, entity.posZ, radius, entity.worldObj.provider.dimensionId, make());
	}

	@Override
	public void sendToAllNear(TileEntity tileEntity, double radius) {
		PacketDispatcher.sendPacketToAllAround(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, radius, tileEntity.worldObj.provider.dimensionId, make());
	}

	@Override
	public void sendToAllTracking(Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, make());
		}
	}

}
