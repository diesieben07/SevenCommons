package de.takeweiland.mods.commons.net2

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

private fun handleCustomPayload(channel: String, buf: ByteBuf, side: Side): Boolean {
    val handler = globalPayloadHandlerRegistry.getHandler(channel) ?: return false
    if (handler.handle(channel, buf, side)) {
        buf.release()
    }
    return true
}

@SideOnly(Side.CLIENT)
internal object ClientInboundCustomPayloadHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg !is SPacketCustomPayload || !handleCustomPayload(msg.channelName, msg.bufferData.buf, Side.CLIENT)) {
            ctx.fireChannelRead(msg)
        }
    }

}

internal object ServerInboundCustomPayloadHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg !is CPacketCustomPayload || !handleCustomPayload(msg.channelName, msg.bufferData.buf, Side.SERVER)) {
            ctx.fireChannelRead(msg)
        }
    }

}