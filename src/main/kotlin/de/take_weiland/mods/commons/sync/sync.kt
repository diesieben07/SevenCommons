package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf
import kotlin.reflect.KProperty

inline fun <CONTAINER: Any> CONTAINER.sync(initialValue: Int) = IntSyncedProperty<CONTAINER>(initialValue)

class IntSyncedProperty<in CONTAINER : Any>(@JvmField var value: Int) : SyncedProperty<Int>() {

    override fun receivePayload(payload: Int) {
        value = payload
    }

    override fun receivePayload(buf: ByteBuf) {
        value = buf.readInt()
    }

    override fun writePayload(buf: ByteBuf, payload: Int) {
        buf.writeInt(payload)
    }

    inline operator fun getValue(obj: CONTAINER, property: KProperty<*>): Int {
        return value
    }

    inline operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: Int) {
        if (value != newValue) {
            value = newValue
            markDirty(obj, newValue)
        }
    }

}