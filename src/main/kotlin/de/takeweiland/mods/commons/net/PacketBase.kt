package de.takeweiland.mods.commons.net

import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
abstract class PacketBase<R> internal constructor() : NetworkSendable<R> {

    open val expectedSize: Int get() = 32

    internal open val channel: String get() = TODO()

    abstract fun write(buf: ByteBuf)

}