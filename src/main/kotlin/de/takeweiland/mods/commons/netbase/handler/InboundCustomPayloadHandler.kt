package de.takeweiland.mods.commons.netbase.handler

import de.takeweiland.mods.commons.netbase.frozenGlobalPayloadHandlerRegistry
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

private fun handleCustomPayload(channel: String, buf: ByteBuf, side: Side, player: EntityPlayer?): Boolean {
    val handler = frozenGlobalPayloadHandlerRegistry[channel] ?: return false
    if (handler.handle(channel, buf, side, player)) {
        buf.release()
    }
    return true
}

@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
internal object ClientInboundCustomPayloadHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg !is SPacketCustomPayload || !handleCustomPayload(msg.channelName, msg.bufferData.buf, Side.CLIENT, null)) {
            ctx.fireChannelRead(msg)
        }
    }

}

internal class ServerInboundCustomPayloadHandler(private val player: EntityPlayerMP) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg !is CPacketCustomPayload || !handleCustomPayload(msg.channelName, msg.bufferData.buf, Side.SERVER, player)) {
            ctx.fireChannelRead(msg)
        }
    }

}