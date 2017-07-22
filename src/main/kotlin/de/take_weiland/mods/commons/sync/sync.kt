package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf
import kotlin.reflect.KProperty


internal const val syncChannel = "SC|Sync"

inline fun <CONTAINER: Any> CONTAINER.sync(initialValue: Int) = IntSyncedProperty<CONTAINER>(initialValue)

class IntSyncedProperty<in CONTAINER : Any>(@JvmField var value: Int) : SyncedProperty<Int>() {
    override fun receivePayload(payload: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writePayload(buf: ByteBuf, data: Int) {
        buf.writeInt(data)
    }

    override fun receivePayload(buf: ByteBuf) {
//        return buf.readInt()
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