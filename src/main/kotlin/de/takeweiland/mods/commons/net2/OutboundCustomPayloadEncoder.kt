package de.takeweiland.mods.commons.net2

import de.takeweiland.mods.commons.net.codec.writeVanillaCompatibleSmallString
import de.takeweiland.mods.commons.net.codec.writerIndex
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise

/**
 * @author Take Weiland
 */
internal abstract class OutboundCustomPayloadEncoder : ChannelOutboundHandlerAdapter() {

    protected abstract val maxSize: Int
    protected abstract val vanillaPacketId: Int

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is CustomPayloadPacket) {
            // re-create the structure of a custom payload packet
            val buf = Unpooled.buffer(msg.expectedSize)
            buf.writeByte(vanillaPacketId)
            buf.writeVanillaCompatibleSmallString(msg.channel)
            val wiBeforeWrite = buf.writerIndex
            msg.write(buf)
            val size = buf.readableBytes()
            if (size > maxSize) {
                val prefix = Unpooled.buffer(1 + 1 + SPLIT_PACKET_CHANNEL_BYTE_LEN)
                prefix.writeByte(vanillaPacketId)
                prefix.writeVanillaCompatibleSmallString(SPLIT_PACKET_CHANNEL)

                val firstPart = buf.retainedSlice(wiBeforeWrite, buf.writerIndex - wiBeforeWrite)
                ctx.write(Unpooled.wrappedBuffer(prefix, firstPart))
            } else {
                ctx.write(buf, promise)
            }
        }
    }

}