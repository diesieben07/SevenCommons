package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.mod.BaseModPacket
import de.take_weiland.mods.commons.net.packet.mod.Packet
import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.simple.sendTo
import de.take_weiland.mods.commons.net.simple.sendToServer
import de.take_weiland.mods.commons.net.writeVarInt
import de.take_weiland.mods.commons.util.Scheduler
import de.take_weiland.mods.commons.util.clientPlayer
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.EnumPacketDirection
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.BiConsumer

/**
 * @author diesieben07
 */
private typealias ResponseTracker = HashMap<Int, CompletableFuture<out Packet.Response>>

internal val responseLogger = LogManager.getLogger("SC|PacketResponses")

internal val clientPendingResponseIds = BitSet()
internal val serverPendingResponseIds = BitSet()
internal val clientPendingResponses = ResponseTracker()
internal val serverPendingResponses = ResponseTracker()

internal fun <R : Packet.Response> handleAsyncResponse(responseId: Int, side: Side, player: EntityPlayer?, future: CompletionStage<out R>) {
    future.whenComplete { r, x ->
        if (x != null) {
            responseLogger.error("Asynchronous response completed with error", x)
        } else if (r == null) {
            responseLogger.error("Asynchronous response completed with null")
        } else {
            WrappedResponse(r, responseId).sendReturn(side, player)
        }
    }
}

internal fun resolvePendingResponse(side: Side, channel: String, responseId: Int, response: Packet.Response) {
    val tracker: ResponseTracker
    val idSet: BitSet
    if (side.isClient) {
        tracker = clientPendingResponses
        idSet = clientPendingResponseIds
    } else {
        tracker = serverPendingResponses
        idSet = serverPendingResponseIds
    }

    idSet.clear(responseId)
    val future = tracker[responseId]
    if (future == null) {
        responseLogger.warn("Received unexpected responseId $responseId on channel $channel.")
        return
    }

    @Suppress("UNCHECKED_CAST")
    (future as CompletableFuture<Packet.Response>).complete(response)
}

internal class WrappedResponse<out R : Packet.Response>(val response: R, val responseId: Int) : CustomPayloadPacket {

    override val channel: String
        get() = response.data.channel

    override fun writePayload(buf: ByteBuf) {
        buf.writePacketId(response.data.packetId)
        buf.writeVarInt(responseId)
        response.write(buf)
    }

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        throw IllegalStateException("Response for remote connection was received locally.")
    }

    fun sendReturn(side: Side, player: EntityPlayer?) {
        if (side.isClient) sendToServer() else sendTo(player as EntityPlayerMP)
    }

}

internal abstract class WrappedPacketWithResponseBase<R : Packet.Response, out P : BaseModPacket>(val packet: P, manager: NetworkManager) : CompletableFuture<R>(), CustomPayloadPacket {

    val client = manager.direction == EnumPacketDirection.CLIENTBOUND

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
        buf.writePacketId(packet.data.packetId)
        buf.writeVarInt(responseId)
        packet.write(buf)
    }

}

internal class WrappedPacketWithResponseAsync<R : Packet.Response, out P : Packet.WithResponse.Async<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager) {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        try {
            complete(packet.receive(side, player))
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }
}

internal class WrappedPacketWithResponse<R : Packet.Response, out P : Packet.WithResponse<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager) {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        Scheduler.forSide(side).run {
            try {
                complete(packet.receive(side, player ?: clientPlayer))
            } catch (x: Throwable) {
                completeExceptionally(x)
            }
        }
    }
}

internal class WrappedPacketWithAsyncResponseAsync<R : Packet.Response, out P : Packet.WithAsyncResponse.Async<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager), BiConsumer<R?, Throwable?> {

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        try {
            packet.receive(side, player).whenComplete(this)
        } catch (x: Throwable) {
            completeExceptionally(x)
        }
    }

    override fun accept(r: R?, x: Throwable?) {
        if (x != null) {
            completeExceptionally(x)
        } else {
            complete(r)
        }
    }
}

internal class WrappedPacketWithAsyncResponse<R : Packet.Response, out P : Packet.WithAsyncResponse<R>>(packet: P, manager: NetworkManager) : WrappedPacketWithResponseBase<R, P>(packet, manager), BiConsumer<R?, Throwable?> {

    override fun accept(r: R?, x: Throwable?) {
        if (x != null) {
            completeExceptionally(x)
        } else {
            complete(r)
        }
    }

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        Scheduler.forSide(side).run {
            try {
                packet.receive(side, player ?: clientPlayer).whenComplete(this)
            } catch (x : Throwable) {
                completeExceptionally(x)
            }
        }
    }
}