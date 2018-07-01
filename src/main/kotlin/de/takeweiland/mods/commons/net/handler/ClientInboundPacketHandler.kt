package de.takeweiland.mods.commons.net.handler

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Take Weiland
 */
@ChannelHandler.Sharable
@SideOnly(Side.CLIENT)
internal object ClientInboundPacketHandler : InboundPacketHandler() {

    override val player: EntityPlayer
        get() = Minecraft.getMinecraft().player

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        handle(ctx, msg, SPacketCustomPayload::getChannelName, SPacketCustomPayload::getBufferData)
    }

}