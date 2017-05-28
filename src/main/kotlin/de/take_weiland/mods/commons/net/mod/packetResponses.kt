package de.take_weiland.mods.commons.net.mod

import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

/**
 * @author diesieben07
 */
internal abstract class BaseWrappedResponsePacket<R : Packet.Response> : CompletableFuture<R>() {

    internal abstract fun executePacketAndComplete(player: EntityPlayer)

}


internal class WrappedPacketWithResponse<R : Packet.Response, out P : Packet.WithResponse<R>>(val packet: P) : BaseWrappedResponsePacket<R>() {

    override fun executePacketAndComplete(player: EntityPlayer) {
        try {
            complete(packet.receive(player))
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }
}

internal class WrappedPacketWithAsyncResposne<R : Packet.Response, out P : Packet.WithResponse.Async<R>>(val packet: P) : BaseWrappedResponsePacket<R>(), BiConsumer<R?, Throwable?> {

    override fun executePacketAndComplete(player: EntityPlayer) {
        try {
            packet.receive(player).whenComplete(this)
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }

    override fun accept(r: R?, x: Throwable?) {
        if (x != null) completeExceptionally(x) else complete(r!!)
    }
}
