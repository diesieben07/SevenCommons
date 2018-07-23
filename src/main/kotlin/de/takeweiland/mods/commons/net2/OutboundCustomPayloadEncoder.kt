package de.takeweiland.mods.commons.net2

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@ChannelHandler.Sharable
@SideOnly(Side.CLIENT)
internal object C2SOutboundEncoder : OutboundCustomPayloadEncoder() {

    override fun vanillaPacket(channel: String, buf: PacketBuffer): Packet<*> = CPacketCustomPayload(channel, buf)

}

@ChannelHandler.Sharable
internal object S2COutboundEncoder : OutboundCustomPayloadEncoder() {

    override fun vanillaPacket(channel: String, buf: PacketBuffer) = SPacketCustomPayload(channel, buf)

}

internal abstract class OutboundCustomPayloadEncoder : ChannelOutboundHandlerAdapter() {

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is CustomPayloadPacket) {
            val buf = ctx.alloc().buffer(msg.expectedSize)
            msg.write(buf)
            ctx.write(vanillaPacket(msg.channel, PacketBuffer(buf)), promise)
        } else {
            ctx.write(msg, promise)
        }
    }

    internal abstract fun vanillaPacket(channel: String, buf: PacketBuffer): Packet<*>

}