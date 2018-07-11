package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.net.base.NetworkSerializable
import de.takeweiland.mods.commons.net.codec.writeVarInt
import de.takeweiland.mods.commons.net.registry.getResponsePacketData
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * @author Take Weiland
 */
abstract class ResponsePacket : PacketBase(), NetworkSerializable {

    @JvmField
    internal var responseId = -1

    final override val channel: String
        get() = getResponsePacketData(javaClass).channel

    final override fun getPacket(packetFactory: (String, ByteBuf) -> AnyMCPacket): AnyMCPacket {
        val data = getResponsePacketData(javaClass)
        val buf = Unpooled.buffer(expectedSize + 2)
        buf.writeVarInt(data.id)
        buf.writeByte(RESPONSE_MARKER_BIT or responseId)
        write(buf)
        return packetFactory(data.channel, buf)
    }
}