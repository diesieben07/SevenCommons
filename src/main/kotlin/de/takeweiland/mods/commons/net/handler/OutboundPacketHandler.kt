package de.takeweiland.mods.commons.net.handler

import de.takeweiland.mods.commons.net.base.NetworkSerializable
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import net.minecraft.network.Packet

/**
 * @author Take Weiland
 */
internal abstract class OutboundPacketHandler : ChannelOutboundHandlerAdapter() {

    protected abstract fun constructPacket(channel: String, buf: ByteBuf): Packet<*>

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is NetworkSerializable) {
            val buf = Unpooled.buffer(msg.expectedSize)
            msg.write(buf)
            ctx.write(constructPacket(msg.channel, buf), promise)
        } else {
            ctx.write(msg, promise)
        }
    }
}