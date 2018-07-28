package de.takeweiland.mods.commons.netbase.handler

import de.takeweiland.mods.commons.netbase.BasicCustomPayloadPacket
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Take Weiland
 */
internal abstract class InboundDirectPayloadHandler : ChannelInboundHandlerAdapter() {

    internal abstract fun BasicCustomPayloadPacket.doHandle()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg is BasicCustomPayloadPacket) {
            msg.doHandle()
        } else {
            ctx.fireChannelRead(msg)
        }
    }
}

@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
internal object ClientInboundDirectPayloadHandler : InboundDirectPayloadHandler() {

    override fun BasicCustomPayloadPacket.doHandle() {
        handle(Minecraft.getMinecraft().player, Side.CLIENT)
    }
}

internal class ServerInboundDirectPayloadHandler(private val player: EntityPlayerMP) : InboundDirectPayloadHandler() {

    override fun BasicCustomPayloadPacket.doHandle() {
        handle(player, Side.SERVER)
    }
}