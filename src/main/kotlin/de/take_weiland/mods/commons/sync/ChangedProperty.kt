package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf
import kotlin.Int as kInt

/**
 * @author diesieben07
 */
abstract class ChangedProperty<T>(val id: kInt) {

    abstract val valueGeneric: T
    abstract fun writeValue(buf: ByteBuf)

    abstract class Obj<T>(id: kInt, val value: T) : ChangedProperty<T>(id) {
        override final val valueGeneric get() = value
    }

    class Int(id: kInt, val value: kInt) : ChangedProperty<kInt>(id) {
        override val valueGeneric get() = value
        override fun writeValue(buf: ByteBuf) {
            buf.writeInt(value)
        }
    }

}

