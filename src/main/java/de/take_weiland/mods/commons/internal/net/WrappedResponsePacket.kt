package de.take_weiland.mods.commons.internal.net

import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet
import net.minecraft.network.NetworkManager

/**
 *
 * Wrapper around a response "R" when not on a local channel
 */
class WrappedResponsePacket<R : Packet.Response> internal constructor(private val response: R, private val packetId: Int, private val uniqueId: Int, private val channel: String) : InternalPacket {

    override fun `_sc$internal$channel`(): String {
        return channel
    }

    override fun `_sc$internal$expectedSize`(): Int {
        return response.expectedSize() + 2
    }

    @Throws(Exception::class)
    override fun `_sc$internal$writeTo`(out: MCDataOutput) {
        out.writeByte(packetId)
        out.writeByte(uniqueId or ResponseSupport.IS_RESPONSE.toInt())
        response.writeTo(out)
    }

    override fun `_sc$internal$receiveDirect`(side: Byte, manager: NetworkManager) {
        NetworkImpl.LOGGER.warn("Channel {} changed from not-local to local, this should be impossible.", channel)
        ResponseSupport.unregister<Packet.Response>(uniqueId).complete(response)
    }

    override fun toString(): String {
        return String.format("Wrapped response packet (packet=%s, packetID=%s, responseId=%s, channel=%s", response, packetId, uniqueId, channel)
    }
}
