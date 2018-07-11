package de.takeweiland.mods.commons.net.handler

import de.takeweiland.mods.commons.net.AnyMCPacket
import de.takeweiland.mods.commons.net.base.NetworkSerializable
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise

/**
 * @author Take Weiland
 */
internal abstract class OutboundPacketHandler : ChannelOutboundHandlerAdapter(), (String, ByteBuf) -> AnyMCPacket {

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is NetworkSerializable) {
            val pkt = msg.getPacket(this)
            ctx.write(pkt, promise)
        } else {
            ctx.write(msg, promise)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        super.exceptionCaught(ctx, cause)
    }
}