package de.takeweiland.mods.commons.net.handler

import io.netty.channel.ChannelHandlerContext
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.client.CPacketCustomPayload

/**
 * @author Take Weiland
 */
internal class ServerInboundPacketHandler(override val player: EntityPlayerMP) : InboundPacketHandler() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        handle(ctx, msg, CPacketCustomPayload::getChannelName, CPacketCustomPayload::getBufferData)
    }
}