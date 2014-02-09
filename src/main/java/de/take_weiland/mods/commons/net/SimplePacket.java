package de.take_weiland.mods.commons.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public interface SimplePacket {

	void sendTo(PacketTarget target);
	
	void sendToServer();

	void sendTo(EntityPlayer player);
	
	void sendTo(Iterable<? extends EntityPlayer> players);

	void sendToAll();

	void sendToAllInDimension(int dimension);

	void sendToAllInDimension(World world);

	void sendToAllNear(World world, double x, double y, double z, double radius);

	void sendToAllNear(int dimension, double x, double y, double z, double radius);

	void sendToAllNear(Entity entity, double radius);

	void sendToAllNear(TileEntity te, double radius);

	void sendToAllTracking(Entity entity);
	
	void sendToAllTracking(TileEntity te);

	void sendToAllAssociated(Entity e);

	void sendToViewing(Container c);
	
	public static final SimplePacket DUMMY = new SimplePacket() {

		@Override
		public void sendToServer() { }

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

		@Override
		public void sendTo(PacketTarget target) { }

		@Override
		public void sendToAllTracking(TileEntity te) { }

		@Override
		public void sendToAllAssociated(Entity e) { }

		@Override
		public void sendToViewing(Container c) { }
	};
	
}
