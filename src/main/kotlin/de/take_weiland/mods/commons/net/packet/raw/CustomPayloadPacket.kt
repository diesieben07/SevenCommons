package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.mod.defaultExpectedPacketSize
import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side

/**
 * A basic custom payload packet. Such a packet will either be received directly via `receiveAsync` or remotely using a
 * `PacketChannel`.
 *
 * Usually the simpler [de.take_weiland.mods.commons.net.packet.Packet] API should be used.
 *
 * @author diesieben07
 */
interface CustomPayloadPacket : SimplePacket {

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

    /**
     * Called when this packet is received directly on a local channel.
     *
     * @param side the logical side receiving this packet
     * @param player the player receiving the packet, may be null on the client.
     */
    fun receiveAsync(side: Side, player: EntityPlayer?)

    override fun sendTo(manager: NetworkManager) {
        manager.channel().writeAndFlush(this)
    }

}