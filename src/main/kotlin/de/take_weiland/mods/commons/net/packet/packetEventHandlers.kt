@file:Mod.EventBusSubscriber(modid = "sevencommons")

/**
 * @author diesieben07
 */
package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.readerIndex
import de.take_weiland.mods.commons.net.writeString
import de.take_weiland.mods.commons.net.writeVarInt
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import net.minecraft.client.Minecraft
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
    setupPipeline(event, SCMessageHanderClient, SCMessageToServerEncoder, SCMessageDecoderClient)
}

@SubscribeEvent
fun serverConnectionFromClient(event: FMLNetworkEvent.ServerConnectionFromClientEvent) {
    val player = (event.handler as NetHandlerPlayServer).player
    setupPipeline(event, SCMessageHandlerServer(player), SCMessageToClientEncoder, SCMessageDecoderServer(player))
}

// encoder: our CustomPayloadPacket -> encoded (ByteBuf) vanilla custom payload packet
// handler: handles directly received CustomPayloadPackets (local channel) and decoded vanilla payload packets
// decoder: quick-path to directly decode the vanilla custom payload data without constructing the Packet object
private fun setupPipeline(event: FMLNetworkEvent<*>, handler: SCMessageHandler, encoder: SCMessageEncoder, decoder: SCMessageDecoder) {
    val pipeline = event.manager.channel().pipeline()
    if (!event.manager.isLocalChannel) {
        pipeline.addAfter("encoder", "sevencommons:encoder", encoder)
        pipeline.addBefore("decoder", "sevencommons:decoder", decoder)
    }
    pipeline.addBefore("packet_handler", "sevencommons:handler", handler)
}

internal fun tryReceive(channel: String, buf: ByteBuf, player: EntityPlayer?): Boolean {
    val packetChannel = globalPacketChannelMap[channel]
    if (packetChannel != null) {
        packetChannel.receive(buf, player)
        return true
    } else {
        return false
    }
}

private abstract class SCMessageHandler : ChannelInboundHandlerAdapter() {

    abstract val player: EntityPlayer?

    final override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        when (msg) {
            is CustomPayloadPacket ->
                msg.receiveAsync(player)
            is SPacketCustomPayload -> if (!tryReceive(msg.channel, msg.data, player)) super.channelRead(ctx, msg)
            is CPacketCustomPayload -> if (!tryReceive(msg.channelName, msg.bufferData, player)) super.channelRead(ctx, msg)
            else -> super.channelRead(ctx, msg)
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        cause.printStackTrace()
        super.exceptionCaught(ctx, cause)
    }
}

@SideOnly(Side.CLIENT)
@ChannelHandler.Sharable
private object SCMessageHanderClient : SCMessageHandler() {
    override val player: EntityPlayer? get() = Minecraft.getMinecraft().player
}

private class SCMessageHandlerServer(override val player: EntityPlayerMP) : SCMessageHandler()

// see EnumConnectionState
private const val c2sCustomPacketId = 9
private const val s2cCustomPacketId = 24

private abstract class SCMessageDecoder : ChannelInboundHandlerAdapter() {

    abstract val player: EntityPlayer?

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        if (msg is ByteBuf) {
            val idx = msg.readerIndex
            val id = msg.getByte(idx).toInt()
            if (id == c2sCustomPacketId || id == s2cCustomPacketId) {
                msg.readerIndex = idx + 1
                val channel = msg.readString()
                if (tryReceive(channel, msg, player)) {
                    return
                } else {
                    msg.readerIndex = idx
                }
            }
        }
        super.channelRead(ctx, msg)
    }

}

private object SCMessageDecoderClient : SCMessageDecoder() {
    override val player: EntityPlayer? get() = Minecraft.getMinecraft().player
}

private class SCMessageDecoderServer(override val player: EntityPlayer) : SCMessageDecoder()

abstract class SCMessageEncoder : ChannelOutboundHandlerAdapter() {

    protected abstract val vanillaPktId: Int

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
        if (msg is CustomPayloadPacket) {
            val channel = msg.channel
            // vanilla packet ID is one VarInt byte
            // channel name length should fit within one VarInt byte
            // channel name is usually all ascii, so one byte per char
            val buf = ctx.alloc().buffer(1 + 1 + channel.length + msg.expectedPayloadSize)

            buf.writeVarInt(vanillaPktId)

            // fake vanilla custom payload packet
            buf.writeString(channel)

            // custom payload data starts here
            msg.writePayload(buf)
            ctx.write(buf, promise)
        } else {
            super.write(ctx, msg, promise)
        }
    }
}
@ChannelHandler.Sharable
object SCMessageToClientEncoder : SCMessageEncoder() {
    override val vanillaPktId: Int
        get() = s2cCustomPacketId

}
@ChannelHandler.Sharable
object SCMessageToServerEncoder : SCMessageEncoder() {
    override val vanillaPktId: Int
        get() = c2sCustomPacketId

}