package de.take_weiland.mods.commons.util

import net.minecraft.util.math.BlockPos

/**
 * @author diesieben07
 */
val BlockPos.chunkX: Int get() = x shr 4
val BlockPos.chunkZ: Int get() = z shr 4