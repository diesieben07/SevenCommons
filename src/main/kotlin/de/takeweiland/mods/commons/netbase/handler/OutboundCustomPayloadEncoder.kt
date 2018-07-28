package de.takeweiland.mods.commons.netbase.handler

import de.takeweiland.mods.commons.netbase.BasicCustomPayloadPacket
import io.netty.buffer.ByteBuf
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
internal object ClientOutboundCustomPayloadEncoder : OutboundCustomPayloadEncoder() {

    override fun invoke(channel: String, buf: ByteBuf): Packet<*> = CPacketCustomPayload(channel, PacketBuffer(buf))

}

@ChannelHandler.Sharable
internal object ServerOutboundCustomPayloadEncoder : OutboundCustomPayloadEncoder() {

    override fun invoke(channel: String, buf: ByteBuf) = SPacketCustomPayload(channel, PacketBuffer(buf))

}

internal abstract class OutboundCustomPayloadEncoder : ChannelOutboundHandlerAdapter(), (String, ByteBuf) -> Packet<*> {

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is BasicCustomPayloadPacket) {
            ctx.write(msg.serialize(this), promise)
        } else {
            ctx.write(msg, promise)
        }
    }

}