package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.util.clientPlayer
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Take Weiland
 */
internal const val MAX_PACKET_ID = 0b0111_1111

internal fun EntityPlayer?.obtainForNetworkOnMainThread(): EntityPlayer = this ?: clientPlayer ?: throw IllegalStateException("Missing EntityPlayer when receiving packet?!")