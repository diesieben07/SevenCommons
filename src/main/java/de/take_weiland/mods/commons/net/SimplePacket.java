package de.take_weiland.mods.commons.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * A Packet which can be send around. The methods correspond to the methods in the {@link net.minecraft.network.packet.Packet} class
 */
public interface SimplePacket {

	SimplePacket sendTo(PacketTarget target);

	SimplePacket sendToServer();

	SimplePacket sendTo(EntityPlayer player);

	SimplePacket sendTo(Iterable<? extends EntityPlayer> players);

	SimplePacket sendToAll();

	SimplePacket sendToAllInDimension(int dimension);

	SimplePacket sendToAllInDimension(World world);

	SimplePacket sendToAllNear(World world, double x, double y, double z, double radius);

	SimplePacket sendToAllNear(int dimension, double x, double y, double z, double radius);

	SimplePacket sendToAllNear(Entity entity, double radius);

	SimplePacket sendToAllNear(TileEntity te, double radius);

	SimplePacket sendToAllTracking(Entity entity);

	SimplePacket sendToAllTracking(TileEntity te);

	SimplePacket sendToAllAssociated(Entity e);

	SimplePacket sendToViewing(Container c);

	/**
	 * A dummy packet which doesn't do anything
	 */
	public static final SimplePacket DUMMY = new SimplePacket() {

		@Override
		public SimplePacket sendToServer() { return this; }

		@Override
		public SimplePacket sendToAllTracking(Entity entity) { return this; }

		@Override
		public SimplePacket sendToAllNear(TileEntity tileEntity, double radius) { return this; }

		@Override
		public SimplePacket sendToAllNear(Entity entity, double radius) { return this; }

		@Override
		public SimplePacket sendToAllNear(int dimension, double x, double y, double z, double radius) { return this; }

		@Override
		public SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) { return this; }

		@Override
		public SimplePacket sendToAllInDimension(World world) { return this; }

		@Override
		public SimplePacket sendToAllInDimension(int dimension) { return this; }

		@Override
		public SimplePacket sendToAll() { return this; }

		@Override
		public SimplePacket sendTo(Iterable<? extends EntityPlayer> players) { return this; }

		@Override
		public SimplePacket sendTo(EntityPlayer player) { return this; }

		@Override
		public SimplePacket sendTo(PacketTarget target) { return this; }

		@Override
		public SimplePacket sendToAllTracking(TileEntity te) { return this; }

		@Override
		public SimplePacket sendToAllAssociated(Entity e) { return this; }

		@Override
		public SimplePacket sendToViewing(Container c) { return this; }
	};
	
}
