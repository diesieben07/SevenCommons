package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.Packet
import de.takeweiland.mods.commons.net.PacketWithResponse
import de.takeweiland.mods.commons.net.ResponsePacket
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
interface ChannelBuilderBase {

    fun <P : Packet> add(id: Int, packet: Class<P>, constructor: (ByteBuf) -> P)

    fun <P : PacketWithResponse<R>, R : ResponsePacket> add(
        id: Int, packet: Class<P>, responsePacket: Class<R>, constructor: (ByteBuf) -> P, responseConstructor: (ByteBuf) -> R
    )

}