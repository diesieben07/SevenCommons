package de.takeweiland.mods.commons.net.handler

import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel
import de.takeweiland.mods.commons.net.base.NetworkSerializable
import de.takeweiland.mods.commons.net.registry.globalNetworkChannels
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
internal abstract class InboundPacketHandler : ChannelInboundHandlerAdapter() {

    protected inline fun <reified P : Packet<*>> handle(ctx: ChannelHandlerContext, msg: Any?, side: Side, player: EntityPlayer?, channelGet: P.() -> String, bufferGet: P.() -> PacketBuffer) {
        if (msg is NetworkSerializable) {
            val channel = globalNetworkChannels[msg.channel] ?: throw IllegalStateException("NetworkSerializable received with unknown channel ${msg.channel} ($msg)")
            @Suppress("UNCHECKED_CAST")
            (channel as AsyncNetworkChannel<NetworkSerializable>).receive(msg, side, player)
        } else if (msg !is P || !handle(msg.channelGet(), msg.bufferGet(), side, player)) {
            ctx.fireChannelRead(msg)
        }
    }

    internal fun handle(channel: String, buf: ByteBuf, side: Side, player: EntityPlayer?): Boolean {
        val nc = globalNetworkChannels[channel] ?: return false
        try {
            nc.receive(buf, side, player)
        } finally {
            buf.release()
        }
        return true
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        super.exceptionCaught(ctx, cause)
        println("EXCEPTION :(")
        cause.printStackTrace()
    }
}