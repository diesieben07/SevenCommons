package de.take_weiland.mods.commons.net

import de.take_weiland.mods.commons.internal.net.*
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.util.Scheduler
import net.minecraft.network.NetworkManager
import java.util.concurrent.CompletionStage
import kotlin.experimental.and

/**
 *
 * A simple interface for implementing custom packets for your Mod. They will be registered using [Network.newSimpleChannel]
 * and will be a sent using the vanilla custom payload packets.
 *
 * An instance of `Packet` must never carry mutable state that is referenced outside of it unless it is ensured that the state
 * can be accessed from multiple threads concurrently.
 *
 * If a packet is send through the local channel on SSP *the same instance* will be received on the other side.
 *
 * `Packet` extends [SimplePacket], the methods defined there are used to send this.

 * @see SimpleChannelBuilder

 * @see SimplePacket


 * @author diesieben07
 */
interface Packet : SimplePacket, PacketBase, InternalPacket, PacketWithData {

    @Deprecated("")
    @Throws(Exception::class)
    override fun `_sc$internal$writeTo`(out: MCDataOutput) {
        out.writeByte(`_sc$internal$getData`().packetId.toInt())
        writeTo(out)
    }

    @Deprecated("")
    override fun `_sc$internal$expectedSize`(): Int {
        return expectedSize() + 1 // packetId
    }

    @Deprecated("")
    override fun `_sc$internal$receiveDirect`(side: Byte, manager: NetworkManager) {
        val data = `_sc$internal$getData`()
        NetworkImpl.validateSide(data.characteristics, side, this)
        if (data.characteristics and Network.ASYNC != 0.toByte()) {
            @Suppress("UNCHECKED_CAST")
            (data.handler as PacketHandler<Packet>).handle(this, NetworkImpl.getPlayer(side, manager))
        } else {

            val handler = data.handler as PacketHandler<*>
            NetworkImpl.getScheduler(side).execute(Scheduler.Task {
                (handler as PacketHandler<Packet>).handle(this, NetworkImpl.getPlayer(side, manager))
                false
            })
        }
    }

    @Deprecated("")
    override fun `_sc$internal$channel`(): String {
        return `_sc$internal$getData`().channel
    }

    /**
     *
     * A version of `Packet` that has a response. The response class needs to implement [de.take_weiland.mods.commons.net.Packet.Response].
     *
     * The response is supplied in form of a [CompletionStage] when an instance of this packet is sent.
     *
     * `Packet.WithResponse` extends [SimplePacket.WithResponse], the methods defined there are used to send this packet.

     * @see SimplePacket.WithResponse
     */
    interface WithResponse<R : Packet.Response> : SimplePacket.WithResponse<R>, PacketBase, PacketWithData {

        override fun sendTo(manager: NetworkManager): CompletionStage<R> {
            val future = AcceptingCompletableFuture<R>()
            NetworkImpl.sendPacket(WrappedPacketWithResponse(this, future), manager)
            return future
        }

    }

    /**
     *
     * The response for a `Packet.WithResponse`.
     */
    interface Response : PacketBase
}
