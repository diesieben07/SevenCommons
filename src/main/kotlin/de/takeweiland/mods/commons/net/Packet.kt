package de.takeweiland.mods.commons.net

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
abstract class Packet : PacketBase<Unit>() {

    abstract fun process(player: EntityPlayer)

    final override fun sendTo(network: NetworkManager) {
        val channel = network.channel()
        channel.write(this, channel.voidPromise())
    }
}