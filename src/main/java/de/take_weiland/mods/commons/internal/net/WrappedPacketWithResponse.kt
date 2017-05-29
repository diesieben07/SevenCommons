package de.take_weiland.mods.commons.internal.net

import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Network
import de.take_weiland.mods.commons.net.Packet
import net.minecraft.network.NetworkManager
import kotlin.experimental.and

/**
 * @author diesieben07
 */
class WrappedPacketWithResponse<P : Packet.WithResponse<R>, R : Packet.Response>(private val original: P, private val future: AcceptingCompletableFuture<R>) : InternalPacket {

    override fun `_sc$internal$receiveDirect`(side: Byte, manager: NetworkManager) {
        val data = original.`_sc$internal$getData`()
        NetworkImpl.validateSide(data.characteristics, side, original)

        val handler = data.handler as PacketHandlerBaseWithResponse<P, R>
        if (data.characteristics and Network.ASYNC == 0.toByte()) {
            NetworkImpl.getScheduler(side).run {
                handler.`_sc$internal$handleInto`(original, future, side, manager)
            }
        } else {
            handler.`_sc$internal$handleInto`(original, future, side, manager)
        }
    }

    override fun `_sc$internal$expectedSize`(): Int {
        return original.expectedSize() + 2
    }

    override fun `_sc$internal$channel`(): String {
        return original.`_sc$internal$getData`().channel
    }

    @Throws(Exception::class)
    override fun `_sc$internal$writeTo`(out: MCDataOutput) {
        val uniqueId = ResponseSupport.register(future).toInt()
        out.writeByte(original.`_sc$internal$getData`().packetId.toInt())
        out.writeByte(uniqueId)

        original.writeTo(out)
    }

    override fun toString(): String {
        return String.format("Wrapped packet with response (packet=%s, future=%s)", original, future)
    }
}
