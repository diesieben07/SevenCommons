package de.takeweiland.mods.commons.netbase

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

/**
 * @author Take Weiland
 */
interface CustomPayloadPacket : BasicCustomPayloadPacket {

    val expectedSize: Int

    val channel: String

    fun writePayload(buf: ByteBuf)

    override fun <R> serialize(factory: (channel: String, buf: ByteBuf) -> R): R {
        val buf = Unpooled.buffer(expectedSize)
        writePayload(buf)
        return factory(channel, buf)
    }
}