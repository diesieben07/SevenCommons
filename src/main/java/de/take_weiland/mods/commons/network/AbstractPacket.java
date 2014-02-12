package de.take_weiland.mods.commons.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import static cpw.mods.fml.common.network.PacketDispatcher.*;
import static de.take_weiland.mods.commons.net.Packets.*;

public abstract class AbstractPacket implements SinglePacket {

	private Packet vanillaPacket;
	
	@Override
	public final Packet make() {
		return vanillaPacket != null ? vanillaPacket : (vanillaPacket = type().transport().make(this));
	}

	@Override
	public int expectedSize() {
		return 32;
	}
	
	@Override
	public void sendTo(PacketTarget target) {
		target.send(make());
	}

	@Override
	public void sendToServer() {
		sendPacketToServer(make());
	}

	@Override
	public void sendTo(EntityPlayer player) {
		sendPacketToPlayer(make(), player);
	}

	@Override
	public void sendTo(Iterable<? extends EntityPlayer> players) {
		sendPacketToPlayers(make(), players);
	}

	@Override
	public void sendToAll() {
		sendPacketToAllPlayers(make());
	}

	@Override
	public void sendToAllInDimension(int dimension) {
		sendPacketToAllInDimension(make(), dimension);
	}

	@Override
	public void sendToAllInDimension(World world) {
		sendPacketToAllInDimension(make(), world.provider.dimensionId);
	}

	@Override
	public void sendToAllNear(World world, double x, double y, double z, double radius) {
		sendPacketToAllAround(x, y, z, radius, world.provider.dimensionId, make());
	}

	@Override
	public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		sendPacketToAllAround(x, y, z, radius, dimension, make());
		
	}

	@Override
	public void sendToAllNear(Entity entity, double radius) {
		sendPacketToAllAround(entity.posX, entity.posY, entity.posZ, radius, entity.worldObj.provider.dimensionId, make());
	}

	@Override
	public void sendToAllNear(TileEntity tileEntity, double radius) {
		sendPacketToAllAround(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, radius, tileEntity.worldObj.provider.dimensionId, make());
	}

	@Override
	public void sendToAllTracking(Entity entity) {
		sendPacketToAllTracking(make(), entity);
	}

}
