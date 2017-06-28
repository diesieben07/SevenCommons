package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.writeVarInt
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.EnumPacketDirection
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

/**
 * @author diesieben07
 */
private typealias ResponseTracker = HashMap<Int, CompletableFuture<out Packet.Response>>

internal val clientPendingResponseIds = BitSet()
internal val serverPendingResponseIds = BitSet()
internal val clientPendingResponses = ResponseTracker()
internal val serverPendingResponses = ResponseTracker()

internal abstract class WrappedPacketWithResponseBase<R : Packet.Response, out P : BasePacket>(val packet: P, manager: NetworkManager) : CompletableFuture<R>(), CustomPayloadPacket {

    val client = manager.direction == EnumPacketDirection.SERVERBOUND

    final override val channel: String
        get() = packet.data.channel

    fun register(): Int {
        val tracker: ResponseTracker
        val idSet: BitSet
        if (client) {
            tracker = clientPendingResponses
            idSet = clientPendingResponseIds
        } else {
            tracker = serverPendingResponses
            idSet = serverPendingResponseIds
        }
        val id = idSet.nextClearBit(0)
        idSet.set(id)
        tracker.put(id, this)
        return id
    }

    override fun writePayload(buf: ByteBuf) {
        val responseId = register()
        buf.writeByte(packet.data.packetId)
        buf.writeVarInt(responseId)
        with(packet) { buf.write() }
    }

}

internal class WrappedPacketWithResponseAsync<R : Packet.Response, out P : Packet.WithResponse.Async<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager) {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        try {
            complete(packet.receive(player))
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }
}

internal class WrappedPacketWithResponse<R : Packet.Response, out P : Packet.WithResponse<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager) {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        player.runPacketOnThread {
            try {
                complete(packet.receive(it))
            } catch (x: Throwable) {
                completeExceptionally(x)
            }
        }
    }
}

internal class WrappedPacketWithAsyncResponseAsync<R : Packet.Response, out P : Packet.WithAsyncResponse.Async<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager), BiConsumer<R?, Throwable?> {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
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

internal class WrappedPacketWithAsyncResponse<R : Packet.Response, out P : Packet.WithAsyncResponse<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager), BiConsumer<R?, Throwable?> {

    override fun accept(r: R?, x: Throwable?) {
        if (x != null) {
            completeExceptionally(x)
        } else {
            complete(r!!)
        }
    }

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        player.runPacketOnThread {
            try {
                packet.receive(it).whenComplete(this)
            } catch (x: Throwable) {
                completeExceptionally(x)
            }
        }
    }
}