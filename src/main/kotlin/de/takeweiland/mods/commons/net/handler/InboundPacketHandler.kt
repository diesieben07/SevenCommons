package de.takeweiland.mods.commons.net.handler

import de.takeweiland.mods.commons.SevenCommons
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer

/**
 * @author Take Weiland
 */
internal abstract class InboundPacketHandler : ChannelInboundHandlerAdapter() {

    internal abstract val player: EntityPlayer

    protected inline fun <reified P : Packet<*>> handle(ctx: ChannelHandlerContext, msg: Any?, channelGet: P.() -> String, bufferGet: P.() -> PacketBuffer) {
        if (msg !is P || !handle(msg.channelGet(), msg.bufferGet(), player)) {
            ctx.fireChannelRead(msg)
        }
    }

    internal fun handle(channel: String, buf: ByteBuf, player: EntityPlayer): Boolean {
        val nc = SevenCommons.networkChannels[channel] ?: return false
        try {
            nc.receive(buf, player)
        } finally {
            buf.release()
        }
        return true
    }
}