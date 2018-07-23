package de.takeweiland.mods.commons.net2

import de.takeweiland.mods.commons.net.codec.readVanillaCompatibleString
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
internal class InboundCustomPayloadHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg is ByteBuf) {
            val vanillaPacketIdByte = msg.getByte(msg.readerIndex()).toInt()

            val receivingSide = when (vanillaPacketIdByte) {
                VANILLA_PAYLOAD_ID_S2C -> Side.CLIENT
                VANILLA_PAYLOAD_ID_C2S -> Side.SERVER
                else -> {
                    ctx.fireChannelRead(msg)
                    return
                }
            }

            val channelByteLen = msg.get

            val channel = msg.readVanillaCompatibleString()

        }
    }
}