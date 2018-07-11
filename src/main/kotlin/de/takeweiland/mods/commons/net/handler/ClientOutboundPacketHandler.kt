package de.takeweiland.mods.commons.net.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
internal object ClientOutboundPacketHandler : OutboundPacketHandler() {
    override fun invoke(channel: String, buf: ByteBuf): Packet<*> {
        return CPacketCustomPayload(channel, PacketBuffer(buf))
    }
}