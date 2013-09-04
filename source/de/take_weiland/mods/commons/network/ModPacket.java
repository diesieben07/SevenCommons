package de.take_weiland.mods.commons.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface ModPacket {

	void sendToServer();

	void sendTo(EntityPlayer player);

	void sendToAll();

	void sendToAllInDimension(int dimension);

	void sendToAllInDimension(World world);

	void sendToAllNear(World world, double x, double y, double z, double radius);

	void sendToAllNear(int dimension, double x, double y, double z, double radius);

	void sendToAllNear(Entity entity, double radius);

	void sendToAllNear(TileEntity tileEntity, double radius);

	void sendToAllTracking(Entity entity);

	void sendToOps();

	void sendTo(Iterable<? extends EntityPlayer> players);
	
	public static final ModPacket DUMMY_PACKET = new ModPacket() {
		
		@Override
		public void sendToServer() { }
		
		@Override
		public void sendToOps() { }
		
		@Override
		public void sendToAllTracking(Entity entity) { }
		
		@Override
		public void sendToAllNear(TileEntity tileEntity, double radius) { }
		
		@Override
		public void sendToAllNear(Entity entity, double radius) { }
		
		@Override
		public void sendToAllNear(int dimension, double x, double y, double z, double radius) { }
		
		@Override
		public void sendToAllNear(World world, double x, double y, double z, double radius) { }
		
		@Override
		public void sendToAllInDimension(World world) { }
		
		@Override
		public void sendToAllInDimension(int dimension) { }
		
		@Override
		public void sendToAll() { }
		
		@Override
		public void sendTo(Iterable<? extends EntityPlayer> players) { }
		
		@Override
		public void sendTo(EntityPlayer player) { }
	};

}