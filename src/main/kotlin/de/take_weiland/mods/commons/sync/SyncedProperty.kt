package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class SyncedProperty<DATA> {

    var id: Int = -1
        get() = if (field >= 0) field else throw IllegalStateException("SyncedProperty not initialized.")
        internal set(value) {
            field = value
        }

    abstract fun writeData(buf: ByteBuf, data: DATA)
    abstract fun readData(buf: ByteBuf): DATA

    protected fun init(property: KProperty<*>) {
        if (property !is KMutableProperty1<*, *>) throw UnsupportedOperationException("Only mutable member properties in a class can be synced.")
        id = property.getPropertyId()
    }

    @PublishedApi
    internal fun accessInit(property: KProperty<*>) = init(property)

}

inline operator fun <T : SyncedProperty<*>> T.provideDelegate(obj: Any?, property: KProperty<*>): T {
    accessInit(property)
    return this
}

interface TickingProperty {

    fun update()

}

internal val changedProperties = HashMap<Any, MutableList<ChangedProperty<*>>>()

private fun changesFor(obj: Any) = with(changedProperties) {
    get(obj) ?: ArrayList<ChangedProperty<*>>().also { put(obj, it) }
}

fun <DATA> SyncedProperty<DATA>.markDirty(obj: Any, newValue: DATA) {
    changesFor(obj) += ChangedProperty.Obj(this, newValue)
}