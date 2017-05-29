package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.util.thread
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

/**
 * @author diesieben07
 */
internal open class WrappedPacketWithResponseAsync<R : Packet.Response, out P : Packet.WithResponse<R>>(val packet: P) : CompletableFuture<R>(), AsyncReceive {

    override fun receiveAsync(player: EntityPlayer) {
        try {
            complete(packet.receive(player))
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }
}

internal class WrappedPacketWithResponse<R : Packet.Response, out P : Packet.WithResponse<R>>(packet: P) : WrappedPacketWithResponseAsync<R, P>(packet) {

    override fun receiveAsync(player: EntityPlayer) {
        player.thread.run {
            super.receiveAsync(player)
        }
    }
}

internal open class WrappedPacketWithAsyncResponseAsync<R : Packet.Response, out P : Packet.WithAsyncResponse<R>>(val packet: P) : CompletableFuture<R>(), BiConsumer<R?, Throwable?>, AsyncReceive {

    override fun receiveAsync(player: EntityPlayer) {
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

internal class WrappedPacketWithAsyncResponse<R : Packet.Response, out P : Packet.WithAsyncResponse<R>>(packet: P) : WrappedPacketWithAsyncResponseAsync<R, P>(packet) {

    override fun receiveAsync(player: EntityPlayer) {
        player.thread.run {
            super.receiveAsync(player)
        }
    }
}