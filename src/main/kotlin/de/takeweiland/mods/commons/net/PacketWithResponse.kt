package de.takeweiland.mods.commons.net

import kotlinx.coroutines.experimental.Deferred
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
abstract class PacketWithResponse<R : ResponsePacket> : PacketBase<Deferred<R>>() {

    abstract fun process(player: EntityPlayer): R

    override fun sendTo(network: NetworkManager): Deferred<R> {
        val channel = network.channel()
        channel.write(this, channel.voidPromise())
        TODO()
    }
}