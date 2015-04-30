package de.take_weiland.mods.commons.net;

import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * <p>An interface defining utility methods for sending packets around.</p>
 * TODO: docs
 */
public interface SimplePacket {

	void sendToServer();

	void sendTo(EntityPlayer player);

	void sendTo(Iterable<? extends EntityPlayer> players);

    void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter);

    default void sendTo(EntityPlayer... players) {
        sendTo(Arrays.asList(players));
    }

    default void sendTo(World world, Predicate<? super EntityPlayer> filter) {
        sendTo(Players.allIn(world), filter);
    }

    default void sendTo(Predicate<? super EntityPlayerMP> filter) {
        sendTo(Players.getAll(), filter);
    }

    default void sendToAll() {
        sendTo(Players.getAll());
    }

    default void sendToAllIn(World world) {
        sendTo(Players.allIn(world));
    }

    default void sendToAllNear(World world, double x, double y, double z, double radius) {
        sendTo(Players.allIn(world), player -> player.getDistanceSq(x, y, z) <= radius);
    }

    default void sendToAllNear(Entity entity, double radius) {
        sendToAllNear(entity.worldObj, entity.posX, entity.posY, entity.posZ, radius);
    }

    default void sendToAllNear(TileEntity te, double radius) {
        sendToAllNear(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, radius);
    }

    default void sendToAllTracking(Entity entity) {
        sendTo(Entities.getTrackingPlayers(entity));
    }

    default void sendToAllAssociated(Entity entity) {
        sendToAllTracking(entity);
        if (entity instanceof EntityPlayer) {
            sendTo((EntityPlayer) entity);
        }
    }

    default void sendToAllTracking(TileEntity te) {
        sendToAllTrackingChunk(te.getWorldObj(), te.xCoord >> 4, te.zCoord >> 4);
    }

    default void sendToAllTracking(Chunk chunk) {
        sendToAllTrackingChunk(chunk.worldObj, chunk.xPosition, chunk.zPosition);
    }

    default void sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
        sendTo(Players.getTrackingChunk(world, chunkX, chunkZ));
    }

    default void sendToViewing(Container c) {
        // the filter makes sure it only contains EntityPlayer's
        //noinspection unchecked
        sendTo((Iterable<? extends EntityPlayer>)
                Iterables.filter(SCReflector.instance.getCrafters(c), it -> it instanceof EntityPlayer));
    }

}
