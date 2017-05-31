package de.take_weiland.mods.commons.net.packet.raw

import net.minecraft.entity.player.EntityPlayer

/**
 * A packet that can be received asynchronously. This interface should not be implemented directly.
 */
interface NettyAsyncReceive {

    fun receiveAsync(player: EntityPlayer)

}

