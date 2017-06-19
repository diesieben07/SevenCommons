package de.take_weiland.mods.commons.sync

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

abstract class SyncedProperty {

    var id: Int = -1
        get() = if (field >= 0) field else throw IllegalStateException("SyncedProperty not initialized.")
        internal set(value) {
            field = value
        }

}

operator fun <T : SyncedProperty> T.provideDelegate(obj: Any?, property: KProperty<*>): T {
    if (property !is KMutableProperty1<*, *>) throw UnsupportedOperationException("Only mutable member properties in a class can be synced.")
    id = property.getPropertyId()
    return this
}

interface TickingProperty {

    fun update()

}

internal val changedProperties = HashMap<Any, MutableList<ChangedProperty<*>>>()

private fun changesFor(obj: Any) = changedProperties.computeIfAbsent(obj) { ArrayList() }

fun <CONTAINER : Any> SyncedProperty.markDirty(obj: CONTAINER, change: ChangedProperty<*>) {
    changedProperties.computeIfAbsent(obj) { ArrayList() } += change
}

fun SyncedProperty.markDirty(obj: Any, newValue: Int) {
    changesFor(obj) += ChangedProperty.Int(id, newValue)
}