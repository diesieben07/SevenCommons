package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.defaultExpectedPacketSize
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

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

    fun receiveAsync(player: EntityPlayer?)

}