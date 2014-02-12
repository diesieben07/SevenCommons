package de.take_weiland.mods.commons.network;

import cpw.mods.fml.common.network.PacketDispatcher;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import static cpw.mods.fml.common.network.PacketDispatcher.*;
import static de.take_weiland.mods.commons.net.Packets.sendPacketToPlayer;

public abstract class AbstractMultipartPacket implements MultipartPacket {

	private Packet[] vanilla;
	
	public final Packet[] make() {
		return vanilla != null ? vanilla : (vanilla = type().transport().makeMulti(this));
	}
	
	@Override
	public int expectedSize() {
		return 50000;
	}

	@Override
	public void sendTo(PacketTarget target) {
		for (Packet p : make()) {
			target.send(p);
		}
	}

	@Override
	public void sendToServer() {
		for (Packet p : make()) {
			sendPacketToServer(p);
		}
	}

	@Override
	public void sendTo(EntityPlayer player) {
		for (Packet p : make()) {
			sendPacketToPlayer(p, player);
		}
	}

	@Override
	public void sendTo(Iterable<? extends EntityPlayer> players) {
		Packet[] pkts = make();
		for (EntityPlayer player : players) {
			for (Packet p : pkts) {
				sendPacketToPlayer(p, player);
			}
		}
	}

	@Override
	public void sendToAll() {
		for (Packet p : make()) {
			sendPacketToAllPlayers(p);
		}
	}

	@Override
	public void sendToAllInDimension(int dimension) {
		for (Packet p : make()) {
			sendPacketToAllInDimension(p, dimension);
		}
	}

	@Override
	public void sendToAllInDimension(World world) {
		sendToAllInDimension(world.provider.dimensionId);
	}

	@Override
	public void sendToAllNear(World world, double x, double y, double z, double radius) {
		sendToAllNear(world.provider.dimensionId, x, y, z, radius);
	}
	
	@Override
	public void sendToAllNear(Entity entity, double radius) {
		sendToAllNear(entity.worldObj.provider.dimensionId, entity.posX, entity.posY, entity.posZ, radius);
	}

	@Override
	public void sendToAllNear(TileEntity tile, double radius) {
		sendToAllNear(tile.worldObj.provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord, radius);
	}

	@Override
	public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		for (Packet p : make()) {
			PacketDispatcher.sendPacketToAllAround(x, y, z, radius, dimension, p);
		}
	}

	@Override
	public void sendToAllTracking(Entity entity) {
		if (Sides.logical(entity).isServer()) {
			EntityTracker et = ((WorldServer)entity.worldObj).getEntityTracker();
			for (Packet p : make()) {
				et.sendPacketToAllPlayersTrackingEntity(entity, p);
			}
		}
	}
}
