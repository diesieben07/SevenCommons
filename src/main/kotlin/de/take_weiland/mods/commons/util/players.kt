package de.take_weiland.mods.commons.util

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer

/**
 * @author diesieben07
 */
inline fun EntityPlayer.requireServer(): EntityPlayerMP = if (isServer) this as EntityPlayerMP else throw IllegalStateException("Expected server-side player")

@Deprecated(message = "This call does nothing, player is already serverside.", replaceWith = ReplaceWith("this"))
inline fun EntityPlayerMP.requireServer(): EntityPlayerMP = this

fun WorldServer.getTrackingPlayers(chunkX: Int, chunkZ: Int): List<EntityPlayerMP> {
    playerChunkMap.getEntry(chunkX, chunkZ).run {
        return if (this == null) emptyList() else players
    }
}