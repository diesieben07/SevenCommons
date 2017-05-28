/**
 * @author diesieben07
 */
package de.take_weiland.mods.commons.net.simple

import de.take_weiland.mods.commons.util.*
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.Container
import net.minecraft.network.NetworkManager
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.FMLCommonHandler

/**
 * Send this packet to the given player.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Sendable<R, *, *>.sendTo(player: EntityPlayerMP): R {
    return sendTo(player.connection.netManager)
}

/**
 * Send this packet to the given player.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Sendable<R, *, *>.sendTo(player: EntityPlayer): R {
    return sendTo((player as EntityPlayerMP).connection.netManager)
}

/**
 * Send this packet to the server.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <R> Sendable<R, *, *>.sendToServer(): R {
    return sendTo(FMLCommonHandler.instance().clientToServerNetworkManager)
}

/* Array */

/**
 * Send this packet to the given players.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(vararg players: EntityPlayer): M {
    return sendMulti {
        for (player in players) {
            sendTo(player)
        }
    }
}

/**
 * Send this packet to the given players.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(vararg players: EntityPlayerMP): M {
    return sendMulti {
        for (player in players) {
            sendTo(player)
        }
    }
}

/**
 * Send this packet to the players in the list that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(vararg players: EntityPlayer, filter: (EntityPlayerMP) -> Boolean): M {
    return sendMulti {
        for (player in players) {
            (player as EntityPlayerMP).let {
                if (filter(it)) sendTo(it)
            }
        }
    }
}

/**
 * Send this packet to the players in the list that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(vararg players: EntityPlayerMP, filter: (EntityPlayerMP) -> Boolean): M {
    return sendMulti {
        @Suppress("LoopToCallChain")
        for (player in players) {
            if (filter(player)) sendTo(player)
        }
    }
}

/* Iterable, there just delegate to the Iterator versions */

/**
 * Send this packet to the given players.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterable<EntityPlayer>): M {
    return sendTo(players.iterator())
}

/**
 * Send this packet to the given players.
 */
@Suppress("NOTHING_TO_INLINE")
@JvmName("sendToPlayersMP")
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterable<EntityPlayerMP>): M {
    return sendTo(players.iterator())
}

/**
 * Send this packet to the players in the list that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterable<EntityPlayer>, filter: (EntityPlayerMP) -> Boolean): M {
    return sendTo(players.iterator(), filter)
}

/**
 * Send this packet to the players in the list that match the given filter.
 */
@JvmName("sendToPlayersMP")
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterable<EntityPlayerMP>, filter: (EntityPlayerMP) -> Boolean): M {
    return sendTo(players.iterator(), filter)
}

/* Iterator */

/**
 * Send this packet to the given players.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterator<EntityPlayer>): M {
    return sendMulti {
        for (player in players) sendTo(player as EntityPlayerMP)
    }
}

/**
 * Send this packet to the given players.
 */
@JvmName("sendToPlayersMP")
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterator<EntityPlayerMP>): M {
    return sendMulti {
        for (player in players) sendTo(player)
    }
}

/**
 * Send this packet to the players in the iterator that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterator<EntityPlayer>, filter: (EntityPlayerMP) -> Boolean): M {
    return sendMulti {
        for (player in players) {
            (player as EntityPlayerMP).let {
                if (filter(it)) sendTo(it)
            }
        }
    }
}

/**
 * Send this packet to the players in the iterator that match the given filter.
 */
@JvmName("sendToPlayersMP")
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(players: Iterator<EntityPlayerMP>, filter: (EntityPlayerMP) -> Boolean): M {
    return sendMulti {
        for (player in players) {
            if (filter(player)) sendTo(player)
        }
    }
}

/* Methods to send to tracking players */

/**
 * Send this packet to all players tracking the chunk with the given coordinates.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTrackingChunk(world: World, chunkX: Int, chunkZ: Int): M {
    return sendToTrackingChunk(world as WorldServer, chunkX, chunkZ)
}

/**
 * Send this packet to all players tracking the chunk with the given coordinates.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTrackingChunk(world: WorldServer, chunkX: Int, chunkZ: Int): M {
    return sendTo(world.getTrackingPlayers(chunkX, chunkZ).iterator())
}

/**
 * Send this packet to all players tracking the given chunk.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTracking(chunk: Chunk): M {
    return sendToTrackingChunk(chunk.world as WorldServer, chunk.xPosition, chunk.zPosition)
}

/**
 * Send this packet to all players tracking the given TileEntity.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTracking(tileEntity: TileEntity): M {
    return sendToTrackingChunk(tileEntity.world as WorldServer, tileEntity.pos.chunkX, tileEntity.pos.chunkZ)
}

/**
 * Send this packet to all players tracking the given entity.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTracking(entity: Entity): M {
    return sendTo(entity.trackingPlayers)
}

/**
 * Send this packet to all players tracking the given entity. If the entity is a player the packet is also send to the
 * player itself.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToAllAssociated(entity: Entity): M {
    return sendMulti {
        sendTo(entity.trackingPlayers)
        if (entity is EntityPlayerMP) sendTo(entity)
    }
}

/**
 * Send this packet to all players tracking the given player entity and the player itself.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToAllAssociated(entity: EntityPlayer): M {
    return sendToAllAssociated(entity as EntityPlayerMP)
}

/**
 * Send this packet to all players tracking the given player entity and the player itself.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToAllAssociated(entity: EntityPlayerMP): M {
    return sendMulti {
        sendTo(entity.trackingPlayers)
        sendTo(entity)
    }
}

/**
 * Send this packet to all players tracking the given container.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToTracking(container: Container): M {
    return sendMulti {
        @Suppress("LoopToCallChain")
        for (listener in container.listeners) if (listener is EntityPlayerMP) sendTo(listener)
    }
}

/* Methods to send to subset of all */

/**
 * Send this packet to all players.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToAll(): M {
    return sendTo(allPlayers)
}

/**
 * Send this packet to all players matching the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(filter: (EntityPlayerMP) -> Boolean): M {
    return sendTo(allPlayers, filter)
}

/**
 * Send this packet to all players in the given world.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(world: World): M {
    return sendTo(world as WorldServer)
}

/**
 * Send this packet to all players in the given world.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(world: WorldServer): M {
    return sendTo(world.players)
}

/**
 * Send this packet to all players in the given world that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(world: World, filter: (EntityPlayerMP) -> Boolean): M {
    return sendTo(world as WorldServer, filter)
}

/**
 * Send this packet to all players in the given world that match the given filter.
 */
inline fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendTo(world: WorldServer, filter: (EntityPlayerMP) -> Boolean): M {
    return sendTo(world.players.iterator(), filter)
}

/**
 * Send this packet to all players within the given radius of the given coordinates.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToNear(world: WorldServer, x: Double, y: Double, z: Double, radius: Double): M {
    return sendTo(world) { player ->
        (radius * radius).let { radiusSq ->
            player.getDistanceSq(x, y, z) <= radiusSq
        }
    }
}

/**
 * Send this packet to all players within the given radius of the given coordinates.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToNear(world: WorldServer, pos: Vec3i, radius: Double): M {
    return sendToNear(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), radius)
}

/**
 * Send this packet to all players within the given radius of the given TileEntity.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToNear(tileEntity: TileEntity, radius: Double): M {
    return sendToNear(tileEntity.world as WorldServer, tileEntity.pos, radius)
}

/**
 * Send this packet to all players within the given radius of the given entity.
 */
fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.sendToNear(entity: Entity, radius: Double): M {
    return sendToNear(entity.world as WorldServer, entity.posX, entity.posY, entity.posZ, radius)
}

fun <R, M, B : MultiResultBuilder<R, M, B>> Sendable<R, M, B>.discardResponse(): SimplePacket {
    return if (this is SimplePacket) this else object : SimplePacket {
        override fun sendTo(manager: NetworkManager) {
            this@discardResponse.sendTo(manager)
        }
    }
}

@Deprecated(message = "This method call is redundant", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("this"))
fun SimplePacket.discardResponse(): SimplePacket {
    return this
}