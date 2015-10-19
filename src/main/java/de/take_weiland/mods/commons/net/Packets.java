package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p>Utility class for sending Packets around.</p>
 */
@ParametersAreNonnullByDefault
public final class Packets {

    /**
     * <p>Send the given packet to the Server.</p>
     *
     * @param packet the packet
     */
    public static void sendToServer(Packet packet) {
        SevenCommons.proxy.sendPacketToServer(packet);
    }

    /**
     * <p>Send the given packet to the given player.</p>
     *
     * @param packet the packet
     * @param player the player
     */
    public static void sendTo(Packet packet, EntityPlayer player) {
        Players.checkNotClient(player).playerNetServerHandler.sendPacket(packet);
    }

    /**
     * <p>Send the given packet to the given players.</p>
     *
     * @param packet  the packet
     * @param players the players
     */
    public static void sendTo(Packet packet, Iterable<? extends EntityPlayer> players) {
        for (EntityPlayer player : players) {
            Players.checkNotClient(player).playerNetServerHandler.sendPacket(packet);
        }
    }

    /**
     * <p>Send the given packet to the given players.</p>
     *
     * @param packet  the packet
     * @param players the players
     */
    public static void sendTo(Packet packet, EntityPlayer... players) {
        for (EntityPlayer player : players) {
            Players.checkNotClient(player).playerNetServerHandler.sendPacket(packet);
        }
    }

    /**
     * <p>Send the given packet to all players currently on the server.</p>
     *
     * @param packet the packet
     */
    public static void sendToAll(Packet packet) {
        sendToList(packet, Players.getAll());
    }

    /**
     * <p>Send the given packet to all players currently in the given world.</p>
     *
     * @param packet the packet
     * @param world  the world
     */
    public static void sendToAllIn(Packet packet, World world) {
        sendToList(packet, Players.allIn(checkNotClient(world)));
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void sendToList(Packet packet, List<EntityPlayerMP> players) {
        for (int i = 0, len = players.size(); i < len; i++) {
            players.get(i).playerNetServerHandler.sendPacket(packet);
        }
    }

    public static void sendTo(Packet packet, Predicate<? super EntityPlayerMP> filter) {
        sendToListFiltered(packet, filter, Players.getAll());
    }

    public static void sendTo(Packet packet, World world, Predicate<? super EntityPlayerMP> filter) {
        sendToListFiltered(packet, filter, Players.allIn(checkNotClient(world)));
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void sendToListFiltered(Packet packet, @Nullable Predicate<? super EntityPlayerMP> filter, List<EntityPlayerMP> all) {
        for (int i = 0, len = all.size(); i < len; i++) {
            EntityPlayerMP player = all.get(i);
            if (filter == null || filter.test(player)) {
                player.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    /**
     * <p>Send the given packet to all players tracking the given entity. If the entity is a player, does not include the player itself.</p>
     *
     * @param packet the packet
     * @param entity the entity
     */
    public static void sendToAllTracking(Packet packet, Entity entity) {
        checkNotClient(entity.worldObj).getEntityTracker().sendToAllTrackingEntity(entity, packet);
    }

    /**
     * <p>Send the given packet to all players tracking the given entity, including the player itself if the entity is a player.</p>
     *
     * @param packet the packet
     * @param entity the entity
     */
    public static void sendToAllAssociated(Packet packet, Entity entity) {
        checkNotClient(entity.worldObj).getEntityTracker().func_151248_b(entity, packet);
    }

    /**
     * <p>Send the given packet to all players tracking the given TileEntity, that is tracking the chunk it is in.</p>
     *
     * @param packet the packet
     * @param te     the TileEntity
     */
    public static void sendToAllTracking(Packet packet, TileEntity te) {
        sendToAllTrackingChunk(packet, te.getWorld(), te.xCoord >> 4, te.zCoord >> 4);
    }

    /**
     * <p>Send the given packet to all players tracking the Chunk at the given coordinates in the given world.</p>
     *
     * @param packet the packet
     * @param world  the world
     * @param chunkX the chunk x coordinate
     * @param chunkZ the chunk z coordinate
     */
    public static void sendToAllTrackingChunk(Packet packet, World world, int chunkX, int chunkZ) {
        Object playerInstance = SCReflector.instance.getPlayerInstance(checkNotClient(world).getPlayerManager(), chunkX, chunkZ, false);
        if (playerInstance != null) {
            SCReflector.instance.sendToAllWatchingChunk(playerInstance, packet);
        }
    }

    /**
     * <p>Send the given packet to all players tracking the given chunk.</p>
     *
     * @param packet the packet
     * @param chunk  the chunk
     */
    public static void sendToAllTracking(Packet packet, Chunk chunk) {
        sendToAllTrackingChunk(packet, chunk.worldObj, chunk.xPosition, chunk.zPosition);
    }

    /**
     * <p>Send the given packet to the player using the given Container.</p>
     * <p><b>Warning</b>: Due to Minecraft's internal design a new Container gets created for every player, even if they are watching the same inventory.
     * This method does <i>not</i> check for players viewing the inventory. As in: this method sends the packet to only one player.</p>
     */
    public static void sendToViewing(Packet packet, Container container) {
        List<ICrafting> crafters = SCReflector.instance.getCrafters(container);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = crafters.size(); i < len; ++i) {
            ICrafting crafter = crafters.get(i);
            if (crafter instanceof EntityPlayerMP) {
                ((EntityPlayerMP) crafter).playerNetServerHandler.sendPacket(packet);
                break;
            }
        }
    }

    /**
     * <p>Send the given packet to all players within the given radius around the given coordinates.</p>
     *
     * @param packet the packet
     * @param world  the world
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param z      the z coordinate
     * @param radius the radius
     */
    public static void sendToAllNear(Packet packet, World world, double x, double y, double z, double radius) {
        List<EntityPlayerMP> players = Players.allIn(checkNotClient(world));
        double radiusSq = radius * radius;

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, len = players.size(); i < len; ++i) {
            EntityPlayerMP player = players.get(i);
            double dx = x - player.posX;
            double dy = y - player.posY;
            double dz = z - player.posZ;
            if (dx * dx + dy * dy + dz * dz < radiusSq) {
                player.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    /**
     * <p>Send the given packet to all players within the given radius around the given entity.</p>
     *
     * @param packet the packet
     * @param entity the entity
     * @param radius the radius
     */
    public static void sendToAllNear(Packet packet, Entity entity, double radius) {
        sendToAllNear(packet, entity.worldObj, entity.posX, entity.posY, entity.posZ, radius);
    }

    /**
     * <p>Send the given packet to all players within the given radius around the given TileEntity.</p>
     *
     * @param packet the packet
     * @param te     the TileEntity
     * @param radius the radius
     */
    public static void sendToAllNear(Packet packet, TileEntity te, double radius) {
        sendToAllNear(packet, te.getWorld(), te.xCoord, te.yCoord, te.zCoord, radius);
    }

    private static WorldServer checkNotClient(World world) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Tried to send packet using a client world");
        }
        return (WorldServer) world;
    }

    private Packets() {
    }
}
