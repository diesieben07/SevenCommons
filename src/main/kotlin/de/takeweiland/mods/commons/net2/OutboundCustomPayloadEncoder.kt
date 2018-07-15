package de.takeweiland.mods.commons.net2

import de.takeweiland.mods.commons.net.codec.writeVanillaCompatibleSmallString
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import kotlin.math.ceil
import kotlin.math.min

/**
 * @author Take Weiland
 */
internal abstract class OutboundCustomPayloadEncoder : ChannelOutboundHandlerAdapter() {

    protected abstract val maxPayloadSize: Int
    protected abstract val vanillaPacketId: Int

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is CustomPayloadPacket) {
            var releasedBuffer = false
            // re-create the structure of a custom payload packet
            val buf = Unpooled.buffer(msg.expectedSize)
            try {
                buf.writeByte(vanillaPacketId)
                buf.writeVanillaCompatibleSmallString(msg.channel)
                msg.write(buf)
                val size = buf.readableBytes()
                if (size > maxPayloadSize) {
                    // we need to split the packet into parts

                    // first: create the prefix that is common to all parts
                    val prefix = Unpooled.buffer(1 + 1 + SPLIT_PACKET_CHANNEL_BYTE_LEN)
                    try {
                        prefix.writeByte(vanillaPacketId)
                        prefix.writeVanillaCompatibleSmallString(SPLIT_PACKET_CHANNEL)

                        var partStartIndex = 1
                        while (partStartIndex < size) {
                            val partLength = min(maxPayloadSize, size - partStartIndex)
                            val part = Unpooled.wrappedBuffer(
                                prefix.retainedSlice(),
                                buf.retainedSlice(partStartIndex, partLength)
                            )
                            ctx.write(part, ctx.voidPromise())
                            partStartIndex += partLength
                        }
                    } finally {
                        prefix.release()
                    }
                } else {
                    ctx.write(buf, promise)
                    releasedBuffer = true
                }
            } finally {
                if (!releasedBuffer) buf.release()
            }
        }
    }

    private fun writeOnePart(ctx: ChannelHandlerContext, buf: ByteBuf, promise: ChannelPromise?) {
        ctx.write(buf, promise)
    }

    private fun writeMultiPartVoidPromise(ctx: ChannelHandlerContext, buf: ByteBuf, size: Int) {
        val prefix = Unpooled.buffer(1 + 1 + SPLIT_PACKET_CHANNEL_BYTE_LEN)
        try {
            prefix.writeByte(vanillaPacketId)
            prefix.writeVanillaCompatibleSmallString(SPLIT_PACKET_CHANNEL)

            var partStartIndex = 1
            while (partStartIndex < size) {
                val partLength = min(maxPayloadSize, size - partStartIndex)
                val part = Unpooled.wrappedBuffer(
                    prefix.retainedSlice(),
                    buf.retainedSlice(partStartIndex, partLength)
                )
                ctx.write(part, ctx.voidPromise())
                partStartIndex += partLength
            }
        } finally {
            prefix.release()
        }
    }

    private class PromiseCountdown(private var count: Int, private val finalPromise: ChannelPromise) : GenericFutureListener<Future<Void>> {

        private var exception: Throwable? = null

        @Synchronized
        override fun operationComplete(future: Future<Void>) {
            if (!future.isSuccess) {
                val ex = exception
                if (ex != null) {
                    ex.addSuppressed(future.cause())
                } else {
                    exception = future.cause()
                }
            }
            if (--count == 0) {
                val ex = exception
                if (ex != null) {
                    finalPromise.setFailure(ex)
                } else {
                    finalPromise.setSuccess()
                }
            }
        }

    }

    private fun writeMultiPartRealPromise(ctx: ChannelHandlerContext, buf: ByteBuf, size: Int, promise: ChannelPromise) {
        val countDown = PromiseCountdown(ceil(size.toDouble() / maxPayloadSize).toInt(), promise)
        val prefix = Unpooled.buffer(1 + 1 + SPLIT_PACKET_CHANNEL_BYTE_LEN)
        try {
            prefix.writeByte(vanillaPacketId)
            prefix.writeVanillaCompatibleSmallString(SPLIT_PACKET_CHANNEL)

            var partStartIndex = 1
            while (partStartIndex < size) {
                val partLength = min(maxPayloadSize, size - partStartIndex)
                val part = Unpooled.wrappedBuffer(
                    prefix.retainedSlice(),
                    buf.retainedSlice(partStartIndex, partLength)
                )
                ctx.write(part).addListener(countDown)
                partStartIndex += partLength
            }
        } finally {
            prefix.release()
        }
    }



}