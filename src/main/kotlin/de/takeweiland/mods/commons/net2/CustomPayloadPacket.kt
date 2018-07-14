package de.takeweiland.mods.commons.net2

import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
interface CustomPayloadPacket {

    val expectedSize: Int

    val channel: String

    fun write(buf: ByteBuf)

}