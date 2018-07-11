package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.util.clientPlayer
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Take Weiland
 */
internal typealias MCPacket<T> = net.minecraft.network.Packet<T>
internal typealias AnyMCPacket = MCPacket<*>

internal fun EntityPlayer?.obtainForNetworkOnMainThread(): EntityPlayer = this ?: clientPlayer ?: throw IllegalStateException("Missing EntityPlayer when receiving packet?!")