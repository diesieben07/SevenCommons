package de.take_weiland.mods.commons.net;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
        sendTo(Iterables.filter(SCReflector.instance.getCrafters(c), EntityPlayer.class));
    }

    interface WithResponse<R> {

        CompletableFuture<R> sendToServer();

        CompletableFuture<R> sendTo(EntityPlayerMP player);

        default CompletableFuture<R> sendTo(EntityPlayer player) {
            return sendTo(Players.checkNotClient(player));
        }

        default Map<EntityPlayerMP, CompletableFuture<R>> sendTo(Iterable<? extends EntityPlayer> players, com.google.common.base.Predicate<? super EntityPlayerMP> filter) {
            return FluentIterable.from(players)
                    .transform(Players::checkNotClient)
                    .filter(filter)
                    .toMap(this::sendTo);

            ImmutableMap.Builder<EntityPlayer, CompletableFuture<R>> b = ImmutableMap.builder();

            for (EntityPlayer p : players) {
                EntityPlayerMP player = Players.checkNotClient(p);
                if (filter == null || filter.test(player)) {
                    b.put(player, sendTo(player));
                }
            }
            return b.build();
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendTo(Iterable<? extends EntityPlayer> players) {
            return sendTo(players, null);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendTo(Predicate<? super EntityPlayerMP> filter) {
            return sendTo(Players.getAll(), filter);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAll() {
            return sendTo(Players.getAll(), null);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllIn(World world) {
            return sendTo(Players.allIn(world), null);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendTo(World world, Predicate<? super EntityPlayerMP> filter) {
            return sendTo(Players.allIn(world), filter);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllNear(Entity entity, double radius) {
            return sendToAllNear(entity.worldObj, entity.posX, entity.posY, entity.posZ, radius);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllNear(TileEntity te, double radius) {
            return sendToAllNear(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, radius);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllNear(World world, double x, double y, double z, double radius) {
            double rSq = radius * radius;
            return sendTo(Players.allIn(world), p -> p.getDistanceSq(x, y, z) <= rSq);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllTracking(TileEntity te) {
            return sendToAllTrackingChunk(te.getWorldObj(), te.xCoord >> 4, te.zCoord >> 4);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllTracking(Chunk chunk) {
            return sendToAllTrackingChunk(chunk.worldObj, chunk.xPosition, chunk.zPosition);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
            return sendTo(Players.getTrackingChunk(world, chunkX, chunkZ), null);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllTracking(Entity entity) {
            return sendTo(Entities.getTrackingPlayers(entity), null);
        }
    }

}
