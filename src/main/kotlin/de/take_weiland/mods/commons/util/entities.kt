package de.take_weiland.mods.commons.util

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer


/**
 * @author diesieben07
 */
@Suppress("UNCHECKED_CAST")
val Entity.trackingPlayers: Iterable<EntityPlayerMP> get() = (world as WorldServer).entityTracker.getTrackingPlayers(this) as Iterable<EntityPlayerMP>