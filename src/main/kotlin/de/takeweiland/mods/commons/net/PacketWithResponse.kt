package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.netbase.SimplePacket
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Deferred
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
abstract class AnyPacketWithResponse<R : ResponsePacket> internal constructor() : PacketBase(), SimplePacket.WithResponse<R>

abstract class PacketWithResponse<R : ResponsePacket> : AnyPacketWithResponse<R>() {

    abstract fun process(player: EntityPlayer): R

    final override fun sendTo(network: NetworkManager): Deferred<R> {
        val channel = network.channel()
        val deferred = CompletableDeferred<R>()
        channel.write(PacketBasePayloadHandler.WithResponseWrapper(this, deferred), channel.voidPromise())
        return deferred
    }

    abstract class Async<R : ResponsePacket> : AnyPacketWithResponse<R>() {

        abstract fun process(player: EntityPlayer): Deferred<R>

        final override fun sendTo(network: NetworkManager): Deferred<R> {
            val channel = network.channel()
            val deferred = CompletableDeferred<R>()
            channel.write(PacketBasePayloadHandler.WithResponseWrapperAsync(this, deferred), channel.voidPromise())
            return deferred
        }

    }

}