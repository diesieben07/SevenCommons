@file:Mod.EventBusSubscriber(modid = "sevencommons")

/**
 * @author diesieben07
 */
package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.raw.NettyAsyncReceive
import de.take_weiland.mods.commons.net.packet.raw.NettyAwarePacket
import de.take_weiland.mods.commons.net.writeString
import de.take_weiland.mods.commons.util.clientPlayer
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.server.SPacketCustomPayload
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

typealias VanillaPacket<T> = net.minecraft.network.Packet<T>

@SubscribeEvent
@SideOnly(Side.CLIENT)
fun clientConnectionToServer(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
    setupPipeline(event, SCMessageHanderClient)
}

@SubscribeEvent
fun serverConnectionFromClient(event: FMLNetworkEvent.ServerConnectionFromClientEvent) {
    setupPipeline(event, SCMessageHandlerServer((event.handler as NetHandlerPlayServer).player))
}

private fun setupPipeline(event: FMLNetworkEvent<*>, handler: SCMessageHandler) {
    val pipeline = event.manager.channel().pipeline()
    if (!event.manager.isLocalChannel) {
        pipeline.addAfter("encoder", "sevencommons:encoder2", SCMessageEncoder)
    }
    pipeline.addBefore("packet_handler", "sevencommons:handler2", handler)
}

private abstract class SCMessageHandler : ChannelInboundHandlerAdapter() {

    abstract val player: EntityPlayer

    final override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        when (msg) {
            is NettyAsyncReceive -> msg.receiveAsync(player)
            is SPacketCustomPayload -> recv(msg.channel, msg.data, ctx, msg)
            is CPacketCustomPayload -> recv(msg.channelName, msg.bufferData, ctx, msg)
            else -> super.channelRead(ctx, msg)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        cause.printStackTrace()
        super.exceptionCaught(ctx, cause)
    }

    private fun recv(channel: String, buf: ByteBuf, ctx: ChannelHandlerContext, msg: Any?) {
        globalPacketChannelMap[channel]?.receive(buf, player) ?: super.channelRead(ctx, msg)
    }

}

private object SCMessageHanderClient : SCMessageHandler() {

    override val player get() = clientPlayer
}

private class SCMessageHandlerServer(override val player: EntityPlayerMP) : SCMessageHandler()

object SCMessageEncoder : ChannelOutboundHandlerAdapter() {

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is NettyAwarePacket) {
            val channel = msg.channel
            // channel name length should fit within one VarInt byte
            // channel name is usually all ascii, so one byte per char
            val buf = ctx.alloc().buffer(1 + channel.length + msg.bufferPreSize)

            // fake vanilla custom payload packet
            buf.writeString(channel)

            // custom payload data starts here
            msg.writeForRemote(buf)
            ctx.write(buf, promise)
        } else {
            super.write(ctx, msg, promise)
        }
    }

}