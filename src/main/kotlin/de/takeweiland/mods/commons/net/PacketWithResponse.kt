package de.takeweiland.mods.commons.net

import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
abstract class PacketWithResponse<R : ResponsePacket> : PacketBase(), SimplePacket.WithResponse<R> {

    abstract fun process(player: EntityPlayer): R

    final override fun sendTo(network: NetworkManager): Deferred<R> {
        val channel = network.channel()
        val deferred = CompletableDeferred<R>()
        channel.write(PacketBaseNetworkChannel.WithResponseWrapper(this, deferred), channel.voidPromise())
        return deferred
    }

}