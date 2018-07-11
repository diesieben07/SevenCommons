package de.takeweiland.mods.commons.net.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import net.minecraft.network.Packet
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.server.SPacketCustomPayload

/**
 * @author Take Weiland
 */
@ChannelHandler.Sharable
internal object ServerOutboundPacketHandler : OutboundPacketHandler() {

    override fun invoke(channel: String, buf: ByteBuf): Packet<*> {
        return SPacketCustomPayload(channel, PacketBuffer(buf))
    }
}