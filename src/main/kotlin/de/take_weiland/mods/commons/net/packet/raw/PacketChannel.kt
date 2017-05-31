package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.DefaultPacketChannelBuilderImpl
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

/**
 * @author diesieben07
 */
interface PacketChannel {

    val channel: String

    fun receive(buf: ByteBuf, player: EntityPlayer)

    companion object {

        inline operator fun invoke(channelName: String, body: DefaultPacketChannelBuilder.() -> Unit) {
            builder(channelName).body()
        }

        fun builder(channelName: String): DefaultPacketChannelBuilder {
            return DefaultPacketChannelBuilderImpl(channelName)
        }

    }

}