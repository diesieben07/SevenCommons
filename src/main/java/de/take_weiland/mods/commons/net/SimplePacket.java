package de.take_weiland.mods.commons.net;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.transformers.net.SimplePacketWithResponseTransformer;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <p>An interface defining utility methods for sending packets around.</p>
 * TODO: docs
 */
public interface SimplePacket {

    void sendToServer();

    void sendTo(EntityPlayerMP player);

    default void sendTo(EntityPlayer player) {
        sendTo(Players.checkNotClient(player));
    }

    default void sendTo(EntityPlayer... players) {
        sendTo(Arrays.asList(players), null);
    }

    default void sendTo(Iterable<? extends EntityPlayer> players) {
        sendTo(players, null);
    }

    default void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        for (EntityPlayer player : players) {
            EntityPlayerMP mp = Players.checkNotClient(player);
            if (filter == null || filter.test(mp)) {
                sendTo(mp);
            }
        }
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
        Iterable<EntityPlayerMP> players = Entities.getTrackingPlayers(entity);
        if (entity instanceof EntityPlayer) {
            Iterable<EntityPlayerMP> playersFinal = players;
            EntityPlayerMP mp = Players.checkNotClient((EntityPlayer) entity);
            players = () -> new OnePlusIterator<>(playersFinal.iterator(), mp);
        }
        sendTo(players);
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
        sendTo(Iterables.filter(SCReflector.instance.getCrafters(c), EntityPlayerMP.class));
    }

    interface WithResponse<R> {

        default SimplePacket discardResponse() {
            /**
             * see {@link SimplePacketWithResponseTransformer}
             */
            return (SimplePacket) this;
        }

        CompletableFuture<R> sendToServer();

        CompletableFuture<R> sendTo(EntityPlayerMP player);

        default CompletableFuture<R> sendTo(EntityPlayer player) {
            return sendTo(Players.checkNotClient(player));
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
            HashMap<EntityPlayerMP, CompletableFuture<R>> map = players instanceof Collection ? Maps.newHashMapWithExpectedSize(((Collection<?>) players).size()) : new HashMap<>();
            Function<EntityPlayerMP, CompletableFuture<R>> sender = this::sendTo;

            for (EntityPlayer player : players) {
                EntityPlayerMP mp = Players.checkNotClient(player);
                if (filter == null || filter.test(mp)) {
                    map.computeIfAbsent(mp, sender);
                }
            }
            return Collections.unmodifiableMap(map);
        }

        default Map<EntityPlayer, CompletableFuture<R>> sendTo(EntityPlayer... players) {
            return sendTo(Arrays.asList(players), null);
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

        default Map<EntityPlayer, CompletableFuture<R>> sendToAllAssociated(Entity entity) {
            Iterable<EntityPlayerMP> players = Entities.getTrackingPlayers(entity);
            if (entity instanceof EntityPlayer) {
                Iterable<EntityPlayerMP> playersFinal = players;
                EntityPlayerMP mp = Players.checkNotClient((EntityPlayer) entity);
                players = () -> new OnePlusIterator<>(playersFinal.iterator(), mp);
            }
            return sendTo(players, null);
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

        default Map<EntityPlayer, CompletableFuture<R>> sendToViewing(Container c) {
            return sendTo(Iterables.filter(SCReflector.instance.getCrafters(c), EntityPlayerMP.class), null);
        }
    }

}
