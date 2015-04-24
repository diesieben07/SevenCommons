package de.take_weiland.mods.commons.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * <p>An interface defining utility methods for sending packets around.</p>
 * TODO: docs
 */
public interface SimplePacket {

	SimplePacket sendToServer();

	SimplePacket sendTo(EntityPlayer player);

	SimplePacket sendTo(Iterable<? extends EntityPlayer> players);

	SimplePacket sendToAll();

	SimplePacket sendToAllIn(World world);

	SimplePacket sendToAllNear(World world, double x, double y, double z, double radius);

	SimplePacket sendToAllNear(Entity entity, double radius);

	SimplePacket sendToAllNear(TileEntity te, double radius);

	SimplePacket sendToAllTracking(Entity entity);

	SimplePacket sendToAllTracking(TileEntity te);

	SimplePacket sendToAllTrackingChunk(World world, int chunkX, int chunkZ);

	SimplePacket sendToAllTracking(Chunk chunk);

	SimplePacket sendToAllAssociated(Entity e);

	SimplePacket sendToViewing(Container c);

}
