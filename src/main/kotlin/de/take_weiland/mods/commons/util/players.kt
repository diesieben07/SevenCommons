package de.take_weiland.mods.commons.util

import de.take_weiland.mods.commons.internal.SevenCommons
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import net.minecraft.world.WorldServer

/**
 * @author diesieben07
 */
inline fun EntityPlayer.requireServer(): EntityPlayerMP = if (isServer) this as EntityPlayerMP else throw IllegalStateException("Expected server-side player")

@Deprecated(message = "This call does nothing, player is already serverside.", replaceWith = ReplaceWith("this"))
inline fun EntityPlayerMP.requireServer(): EntityPlayerMP = this

val allPlayers get(): List<EntityPlayerMP> = serverInstance.playerList.players

val clientPlayer get(): EntityPlayer = SevenCommons.proxy.clientPlayer

fun WorldServer.getTrackingPlayers(chunkX: Int, chunkZ: Int): List<EntityPlayerMP> {
    return playerChunkMap.getEntry(chunkX, chunkZ)?.players ?: emptyList()
}

val World.players get(): List<EntityPlayer> = playerEntities
@Suppress("UNCHECKED_CAST")
val WorldServer.players get(): List<EntityPlayerMP> = playerEntities as List<EntityPlayerMP>