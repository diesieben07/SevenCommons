package de.takeweiland.mods.commons.net2

import de.takeweiland.mods.commons.net.codec.writeVanillaCompatibleSmallString
import de.takeweiland.mods.commons.net.codec.writerIndex
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise

/**
 * @author Take Weiland
 */
internal object C2SOutboundEncoder : OutboundCustomPayloadEncoder() {

    override val maxPayloadSize: Int
        get() = MAX_PAYLOAD_C2S

    override val vanillaPacketId: Int
        get() = VANILLA_PAYLOAD_ID_C2S
}

internal object S2COutboundEncoder : OutboundCustomPayloadEncoder() {

    override val maxPayloadSize: Int
        get() = MAX_PAYLOAD_S2C

    override val vanillaPacketId: Int
        get() = VANILLA_PAYLOAD_ID_S2C
}

internal abstract class OutboundCustomPayloadEncoder : ChannelOutboundHandlerAdapter() {

    protected abstract val maxPayloadSize: Int
    protected abstract val vanillaPacketId: Int

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise) {
        if (msg is CustomPayloadPacket) {
            // re-create the structure of a custom payload packet
            val buf = ctx.alloc().buffer(1 + msg.channel.length + msg.expectedSize)
            buf.writeByte(vanillaPacketId)
            buf.writeVanillaCompatibleSmallString(msg.channel)

            val wi = buf.writerIndex
            msg.write(buf)
            val written = buf.writerIndex - wi
            check(written <= maxPayloadSize) { "Can only send $maxPayloadSize bytes" }
            ctx.write(buf, promise)
        } else {
            ctx.write(msg, promise)
        }
    }

}