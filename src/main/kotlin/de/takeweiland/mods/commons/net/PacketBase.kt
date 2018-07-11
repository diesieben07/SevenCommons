package de.takeweiland.mods.commons.net

import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
abstract class PacketBase {

    val expectedSize: Int get() = 32

    abstract fun write(buf: ByteBuf)

}