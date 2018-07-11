package de.takeweiland.mods.commons.net.handler

import io.netty.channel.ChannelHandlerContext
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
internal class ServerInboundPacketHandler(private val player: EntityPlayer?) : InboundPacketHandler() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        handle(ctx, msg, Side.SERVER, player, CPacketCustomPayload::getChannelName, CPacketCustomPayload::getBufferData)
    }
}