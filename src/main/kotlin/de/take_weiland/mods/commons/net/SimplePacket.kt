package de.take_weiland.mods.commons.net

import com.google.common.collect.Iterables
import com.google.common.collect.Iterators
import de.take_weiland.mods.commons.internal.CommonMethodHandles
import de.take_weiland.mods.commons.util.Entities
import de.take_weiland.mods.commons.util.Players
import de.take_weiland.mods.commons.util.getTrackingPlayers
import de.take_weiland.mods.commons.util.requireServer
import gnu.trove.map.hash.THashMap
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.inventory.IContainerListener
import net.minecraft.network.NetworkManager
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.FMLCommonHandler

import java.util.Collections
import java.util.concurrent.CompletionStage
import java.util.function.Predicate

/**
 *
 * A packet that can be sent to various targets.
 *
 * When implementing this interface, at least [.sendToServer] and [.sendTo] must be implemented. If multiple players can be targeted efficiently
 * [.sendTo] should be overridden as well.
 */
interface SimplePacket {

    fun sendTo(manager: NetworkManager)

    /**
     *
     * Send this packet to the server. This method must only be called from the client thread.
     */
    fun sendToServer() {
        sendTo(FMLCommonHandler.instance().clientToServerNetworkManager)
    }

    /**
     * Send this packet to the given player.
     * @param player the player
     */
    fun sendTo(player: EntityPlayerMP) {
        sendTo(player.connection.netManager)
    }

    /**
     *
     * Send this packet to the players in the given world matching the filter or to all players in the given world if the filter is null. This method must only be called for server-side worlds.

     * @param world  the world
     * *
     * @param filter the filter, may be null
     */
    fun sendTo(world: World, filter: Predicate<in EntityPlayer>) {
        sendTo(Players.allIn(world), filter)
    }

    /**
     *
     * Send this packet to all players matching the filter or to all players if the filter is null.

     * @param filter the filter, may be null
     */
    fun sendTo(filter: Predicate<in EntityPlayerMP>) {
        sendTo(Players.getAll(), filter)
    }

    /**
     *
     * Send this packet to all players.
     */
    fun sendToAll() {
        sendTo(Players.getAll())
    }

    /**
     *
     * Send this packet to all players in the given world. This method must only be called for server-side worlds.

     * @param world the world
     */
    fun sendToAllIn(world: World) {
        sendTo(Players.allIn(world))
    }

    /**
     *
     * Send this packet to all players that are within the given radius around the given point. This method must only be called for server-side worlds.

     * @param world  the world
     * *
     * @param x      the x coordinate
     * *
     * @param y      the y coordinate
     * *
     * @param z      the z coordinate
     * *
     * @param radius the radius
     */
    fun sendToAllNear(world: World, x: Double, y: Double, z: Double, radius: Double) {
        sendTo(Players.allIn(world), { player -> player.getDistanceSq(x, y, z) <= radius })
    }

    fun sendToAllNear(world: World, pos: BlockPos, radius: Double) {
        sendToAllNear(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), radius)
    }

    /**
     *
     * Send this packet to all players that are within the given radius around the given entity. This method must only be called for server-side entities.
     *
     * Usually [.sendToAllTracking] or [.sendToAllAssociated] are more sensible choices.

     * @param entity the entity
     * *
     * @param radius the radius
     */
    fun sendToAllNear(entity: Entity, radius: Double) {
        sendToAllNear(entity.world, entity.posX, entity.posY, entity.posZ, radius)
    }

    /**
     *
     * Send this packet to all players that are within the given radius around the given TileEntity. This method must only be called for server-side TileEntities.
     *
     * Usually [.sendToAllTracking] is a more sensible choice.

     * @param te     the TileEntity
     * *
     * @param radius the radius
     */
    fun sendToAllNear(te: TileEntity, radius: Double) {
        sendToAllNear(te.world, te.pos, radius)
    }

    /**
     *
     * Send this packet to all players that are tracking the given entity. This method must only be called for server-side entities.
     *
     * *Note:* Players do not track themselves. See [.sendToAllAssociated].

     * @param entity the entity
     */
    fun sendToAllTracking(entity: Entity) {
        sendTo(Entities.getTrackingPlayers(entity))
    }

    /**
     *
     * Send this packet to all players that are tracking the given entity. Additionally if the entity is a player also sends the packet to the player itself. This method must only be called for server-side entities.

     * @param entity the entity
     */
    fun sendToAllAssociated(entity: Entity) {
        var players = Entities.getTrackingPlayers(entity).iterator()
        if (entity is EntityPlayerMP) {
            players = OnePlusIterator(players, entity)
        }
        sendTo(players)
    }

    /**
     *
     * Send this packet to all players that are tracking the given TileEntity. A player is tracking a TileEntity when the TileEntity is loaded for them ("within view-distance").
     * This method must only be called for server-side TileEntities.

     * @param te the TileEntity
     */
    fun sendToAllTracking(te: TileEntity) {
        val pos = te.pos
        sendToAllTrackingChunk(te.world, pos.x shr 4, pos.z shr 4)
    }

    /**
     *
     * Send this packet to all players that are tracking the given chunk. A player is tracking a chunk when the chunk is loaded for them ("within view-distance").
     * This method must only be called for server-side chunk.

     * @param chunk the Chunk
     */
    fun sendToAllTracking(chunk: Chunk) {
        sendToAllTrackingChunk(chunk.world, chunk.xPosition, chunk.zPosition)
    }

    /**
     *
     * Send this packet to all players that are tracking the given chunk. A player is tracking a chunk when the chunk is loaded for them ("within view-distance").
     * This method must only be called for server-side worlds.

     * @param world  the world
     * *
     * @param chunkX the x coordinate of the chunk
     * *
     * @param chunkZ the z coordinate of the chunk
     */
    fun sendToAllTrackingChunk(world: World, chunkX: Int, chunkZ: Int) {
        sendTo(Players.getTrackingChunk(world, chunkX, chunkZ))
    }

    /**
     *
     * Send this packet to all players that are looking at the given container. This will most likely never be more than one.

     * @param c the container
     */
    fun sendToViewing(c: Container) {
        val listeners = CommonMethodHandles.getListeners(c)
        for (listener in listeners) {
            if (listener is EntityPlayerMP) {
                sendTo(listener)
            }
        }
    }

    /**
     *
     * A version of [SimplePacket] with a Response.
     *
     * A response is supplied in form of a [CompletionStage&amp;lt;R&amp;gt;][CompletionStage] or in case of a method that sends to
     * multiple players a `Map&lt;EntityPlayer, CompletionStage&lt;R&gt;&gt;`.
     */
    interface WithResponse<R> {

        /**
         *
         * Return a `SimplePacket` version of this packet, where the response is ignored. Use this method whenever a `SimplePacket` is expected.

         * @return a `SimplePacket` version
         */
        fun discardResponse(): SimplePacket {
            return PacketDiscardedResponse(this)
        }

        fun sendTo(manager: NetworkManager): CompletionStage<R>

        /**
         *
         * Send this packet to the server. This method must only be called from the client thread.

         * @return a `CompletableFuture` representing the response
         */
        fun sendToServer(): CompletionStage<R> {
            return sendTo(FMLCommonHandler.instance().clientToServerNetworkManager)
        }

        /**
         *
         * Send this packet to the given player.

         * @param player the player
         * *
         * @return a `CompletableFuture` representing the response
         */
        fun sendTo(player: EntityPlayerMP): CompletionStage<R> {
            return sendTo(player.connection.netManager)
        }

        /**
         *
         * Send this packet to the given player. This method must only be called for server-side players.

         * @param player the player
         * *
         * @return a `CompletableFuture` representing the response
         */
        fun sendTo(player: EntityPlayer): CompletionStage<R> {
            return sendTo(Players.checkNotClient(player))
        }

        /**
         *
         * Send this packet to the given players. This method must only be called for server-side players.

         * @param players the players
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendTo(vararg players: EntityPlayer): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Iterators.forArray(*players), null)
        }

        /**
         *
         * Send this packet to the given players. This method must only be called for server-side players.

         * @param players the players
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendTo(players: Iterable<EntityPlayer>): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(players.iterator(), null)
        }

        /**
         *
         * Send this packet to the players in the collection matching the filter or to all players in the collection if the filter is null. This method must only be called for server-side players.

         * @param players the players
         * *
         * @param filter  the filter, may be null
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendTo(players: Iterable<EntityPlayer>, filter: Predicate<in EntityPlayerMP>?): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(players.iterator(), filter)
        }

        /**
         *
         * Send this packet to the players in the iterator matching the filter or to all players in the iterator if the filter is null. This method must only be called for server-side players.

         * @param players the players
         * *
         * @param filter  the filter, may be null
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        @JvmOverloads fun sendTo(players: Iterator<EntityPlayer>, filter: Predicate<in EntityPlayerMP>? = null): Map<EntityPlayerMP, CompletionStage<R>> {
            val map = THashMap<EntityPlayerMP, CompletionStage<R>>()

            while (players.hasNext()) {
                val mp = Players.checkNotClient(players.next())
                if (filter == null || filter.test(mp)) {
                    if (!map.containsKey(mp)) {
                        map.put(mp, sendTo(mp))
                    }
                }
            }
            return Collections.unmodifiableMap(map)
        }

        /**
         *
         * Send this packet to the players in the given world matching the filter or to all players in the given world if the filter is null. This method must only be called for server-side worlds.

         * @param world  the world
         * *
         * @param filter the filter, may be null
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendTo(world: World, filter: Predicate<in EntityPlayerMP>): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Players.allIn(world), filter)
        }

        /**
         *
         * Send this packet to all players matching the filter or to all players if the filter is null.

         * @param filter the filter, may be null
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendTo(filter: Predicate<in EntityPlayerMP>): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Players.getAll(), filter)
        }

        /**
         *
         * Send this packet to all players.

         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAll(): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Players.getAll(), null)
        }

        /**
         *
         * Send this packet to all players in the given world. This method must only be called for server-side worlds.

         * @param world the world
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllIn(world: World): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Players.allIn(world), null)
        }

        /**
         *
         * Send this packet to all players that are within the given radius around the given point. This method must only be called for server-side worlds.

         * @param world  the world
         * *
         * @param x      the x coordinate
         * *
         * @param y      the y coordinate
         * *
         * @param z      the z coordinate
         * *
         * @param radius the radius
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllNear(world: World, x: Double, y: Double, z: Double, radius: Double): Map<EntityPlayerMP, CompletionStage<R>> {
            val rSq = radius * radius
            return sendTo(Players.allIn(world), { p -> p.getDistanceSq(x, y, z) <= rSq })
        }

        fun sendToAllNear(world: World, pos: Vec3i, radius: Double): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendToAllNear(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), radius)
        }

        /**
         *
         * Send this packet to all players that are within the given radius around the given entity. This method must only be called for server-side entities.
         *
         * Usually [.sendToAllTracking] or [.sendToAllAssociated] are more sensible choices.

         * @param entity the entity
         * *
         * @param radius the radius
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllNear(entity: Entity, radius: Double): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendToAllNear(entity.world, entity.posX, entity.posY, entity.posZ, radius)
        }

        /**
         *
         * Send this packet to all players that are within the given radius around the given TileEntity. This method must only be called for server-side TileEntities.
         *
         * Usually [.sendToAllTracking] is a more sensible choice.

         * @param te     the TileEntity
         * *
         * @param radius the radius
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllNear(te: TileEntity, radius: Double): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendToAllNear(te.world, te.pos, radius)
        }

        /**
         *
         * Send this packet to all players that are tracking the given entity. This method must only be called for server-side entities.
         *
         * *Note:* Players do not track themselves. See [.sendToAllAssociated].

         * @param entity the entity
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllTracking(entity: Entity): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Entities.getTrackingPlayers(entity), null)
        }

        /**
         *
         * Send this packet to all players that are tracking the given entity. Additionally if the entity is a player also sends the packet to the player itself. This method must only be called for server-side entities.

         * @param entity the entity
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllAssociated(entity: Entity): Map<EntityPlayerMP, CompletionStage<R>> {
            var players: Iterable<EntityPlayerMP> = Entities.getTrackingPlayers(entity)
            if (entity is EntityPlayer) {
                val playersFinal = players
                val mp = Players.checkNotClient(entity)
                players = { OnePlusIterator(playersFinal.iterator(), mp) }
            }
            return sendTo(players, null)
        }

        /**
         *
         * Send this packet to all players that are tracking the given TileEntity. A player is tracking a TileEntity when the TileEntity is loaded for them ("within view-distance").
         * This method must only be called for server-side TileEntities.

         * @param te the TileEntity
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllTracking(te: TileEntity): Map<EntityPlayerMP, CompletionStage<R>> {
            val pos = te.pos
            return sendToAllTrackingChunk(te.world, pos.x shr 4, pos.z shr 4)
        }

        /**
         *
         * Send this packet to all players that are tracking the given chunk. A player is tracking a chunk when the chunk is loaded for them ("within view-distance").
         * This method must only be called for server-side chunk.

         * @param chunk the Chunk
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllTracking(chunk: Chunk): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendToAllTrackingChunk(chunk.world, chunk.xPosition, chunk.zPosition)
        }

        /**
         *
         * Send this packet to all players that are tracking the given chunk. A player is tracking a chunk when the chunk is loaded for them ("within view-distance").
         * This method must only be called for server-side worlds.

         * @param world  the world
         * *
         * @param chunkX the x coordinate of the chunk
         * *
         * @param chunkZ the z coordinate of the chunk
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToAllTrackingChunk(world: World, chunkX: Int, chunkZ: Int): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Players.getTrackingChunk(world, chunkX, chunkZ), null)
        }

        /**
         *
         * Send this packet to all players that are looking at the given container. This will most likely never be more than one.

         * @param c the container
         * *
         * @return a `CompletableFuture` for each player representing their response
         */
        fun sendToViewing(c: Container): Map<EntityPlayerMP, CompletionStage<R>> {
            return sendTo(Iterables.filter(CommonMethodHandles.getListeners(c), EntityPlayerMP::class.java), null)
        }
    }

    /**
     *
     * Send this packet to the given players. This method must only be called for server-side players.

     * @param players the players
     * *
     * @return a `CompletableFuture` for each player representing their response
     */

    companion object {

        fun of(packet: net.minecraft.network.Packet<*>): SimplePacket {
            return { manager -> manager.sendPacket(packet) }
        }
    }

}

fun SimplePacket.sendTo(player: EntityPlayer) {
    sendTo(player.requireServer())
}

/* Array */

fun SimplePacket.sendTo(vararg players: EntityPlayer) {
    for (player in players) sendTo(player.requireServer())
}

fun SimplePacket.sendTo(vararg players: EntityPlayerMP) {
    for (player in players) sendTo(player)
}

inline fun SimplePacket.sendTo(vararg players: EntityPlayer, filter: (EntityPlayerMP) -> Boolean) {
    for (player in players) {
        player.requireServer().let {
            if (filter(it)) sendTo(it)
        }
    }
}

inline fun SimplePacket.sendTo(vararg players: EntityPlayerMP, filter: (EntityPlayerMP) -> Boolean) {
    for (player in players) {
        if (filter(player)) sendTo(player)
    }
}

/* Iterable, there just delegate to the Iterator versions */

inline fun SimplePacket.sendTo(players: Iterable<EntityPlayer>) {
    sendTo(players.iterator())
}

@JvmName("sendToPlayersMP")
inline fun SimplePacket.sendTo(players: Iterable<EntityPlayerMP>) {
    sendTo(players.iterator())
}

inline fun SimplePacket.sendTo(players: Iterable<EntityPlayer>, filter: (EntityPlayerMP) -> Boolean) {
    sendTo(players.iterator(), filter)
}

@JvmName("sendToPlayersMP")
inline fun SimplePacket.sendTo(players: Iterable<EntityPlayerMP>, filter: (EntityPlayerMP) -> Boolean) {
    sendTo(players.iterator(), filter)
}

/* Iterator */

fun SimplePacket.sendTo(players: Iterator<EntityPlayer>) {
    for (player in players) sendTo(player.requireServer())
}

@JvmName("sendToPlayersMP")
fun SimplePacket.sendTo(players: Iterator<EntityPlayerMP>) {
    for (player in players) sendTo(player)
}

inline fun SimplePacket.sendTo(players: Iterator<EntityPlayer>, filter: (EntityPlayerMP) -> Boolean) {
    for (player in players) {
        player.requireServer().let {
            if (filter(it)) sendTo(it)
        }
    }
}

@JvmName("sendToPlayersMP")
inline fun SimplePacket.sendTo(players: Iterator<EntityPlayerMP>, filter: (EntityPlayerMP) -> Boolean) {
    for (player in players) {
        if (filter(player)) sendTo(player)
    }
}

/* Methods to send to tracking players */

inline fun SimplePacket.sendToTrackingChunk(world: WorldServer, chunkX: Int, chunkZ: Int) {
    sendTo(world.getTrackingPlayers(chunkX, chunkZ).iterator())
}

fun Chunk.sendToTracking(packet: SimplePacket) {
    packet.sendTo(world.requireServer().getTrackingPlayers(xPosition, zPosition))
}