package de.takeweiland.mods.commons.util

import de.takeweiland.mods.commons.SevenCommons
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldServer

/**
 * @author Take Weiland
 */
val clientPlayer: EntityPlayer? get() = SevenCommons.proxy.clientPlayer

val MinecraftServer.allPlayers: List<EntityPlayerMP> get() = this.playerList.players

val Entity.trackingPlayers: Collection<EntityPlayerMP> get() = (world as WorldServer).entityTracker.trackedEntityHashTable.lookup(entityId)?.trackingPlayers ?: emptySet()