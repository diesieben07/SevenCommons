package de.takeweiland.mods.commons.net.base

import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
interface NetworkSerializable {

    fun write(buf: ByteBuf)

    val channel: String

    val expectedSize: Int

}