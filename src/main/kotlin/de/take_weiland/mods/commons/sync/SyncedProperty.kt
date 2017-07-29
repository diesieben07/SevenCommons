package de.take_weiland.mods.commons.sync

import io.netty.buffer.ByteBuf
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class SyncedProperty<PAYLOAD> {

    var id: Int = -1
        get() = if (field >= 0) field else throw IllegalStateException("SyncedProperty not initialized.")
        internal set(value) {
            field = value
        }

    abstract fun writePayload(buf: ByteBuf, data: PAYLOAD)

    abstract fun receivePayload(buf: ByteBuf)
    abstract fun receivePayload(payload: PAYLOAD)

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

internal val changedProperties = HashMap<Any, ChangedPropertyList<*>>()

private fun changesFor(obj: Any): ChangedPropertyList<*> = with(changedProperties) {
    val changes = this[obj]
    if (changes != null) return changes

    val newChangeList = findContainerType(obj).createChangedPropertyList(obj)
    this[obj] = newChangeList
    return newChangeList
}

fun <PAYLOAD> SyncedProperty<PAYLOAD>.markDirty(obj: Any, newValue: PAYLOAD) {
    changesFor(obj).addChange(this, newValue)
}