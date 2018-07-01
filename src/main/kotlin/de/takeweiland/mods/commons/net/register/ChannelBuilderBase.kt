package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.Packet
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
interface ChannelBuilderBase {

    fun <P : Packet> add(id: Int, packet: Class<P>, constructor: (ByteBuf) -> P)

}