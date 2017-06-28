package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.defaultExpectedPacketSize
import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
interface CustomPayloadPacket {

    /**
     * The channel this packet should be send on
     */
    val channel: String

    /**
     * The expected size for the payload of this packet.
     */
    val expectedPayloadSize: Int
        get() = defaultExpectedPacketSize

    /**
     * Write the payload to the given buffer, starting at it's current writer index. After this method returns the writer
     * index should point at the index behind the payload.
     * The reader index of the buffer must not be modified by this method.
     *
     * @param buf the buffer to write to
     */
    fun writePayload(buf: ByteBuf)

    fun receiveAsync(side: Side, player: EntityPlayer?)

    interface Sendable : CustomPayloadPacket, SimplePacket {

        override fun sendTo(manager: NetworkManager) {
            manager.channel().writeAndFlush(this)
        }
    }

}