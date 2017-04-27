package de.take_weiland.mods.commons.sync

import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

abstract class BaseSyncedProperty internal constructor() {

    @JvmField
    var property: KProperty1<*, *>? = null
    @JvmField
    var obj: Any? = null

    internal var id0: Int = -1
    internal inline fun getId(): Int = id0.let {
        if (it == -1) idCache.computeIfAbsent(property, {
            SyncedPropertyJavaHelper.getPropertyId(property, obj)
        }) else it
    }

    fun init(obj: Any, property: KProperty<*>) {
        this.property = property as? KProperty1<*, *> ?: throw UnsupportedOperationException("Cannot sync static field")
        this.obj = obj
    }

    inline operator fun <T : BaseSyncedProperty> T.provideDelegate(obj: Any, property: KProperty<*>): T {
        init(obj, property)
        return this
    }

}

// ID is built as follows:
//      - Lowest 3 bits are class ID, starting with 0 at the top. Top is the class extending from e.g. TileEntity.
//        Classes without synced properties are still counted
//        This allows for a 7-deep hierarchy, which should be plenty
//      - Rest is property id, in order of definition in the source file.
// This allows classes to add synced properties without changing other classes' IDs
// The ID will fit in one VarInt byte so long as there are no more than 16 synced properties in a class
// Longer IDs appear on a per-class basis, so only classes with more than 16 properties get longer IDs.

private val idCache = ConcurrentHashMap<KProperty1<*, *>?, Int>()

abstract class SyncedProperty<T, R : ICapabilityProvider> : BaseSyncedProperty() {

    abstract operator fun getValue(obj: R, property: KProperty<*>): T
    abstract operator fun setValue(obj: R, property: KProperty<*>, newValue: T)

}



private fun ICapabilityProvider.getSyncCapability() = getCapability(SyncCapHolder.SYNC_CAP_KEY, null)!!

fun <C : ICapabilityProvider> BaseSyncedProperty.markDirty(self: C) {
    self.getSyncCapability().markDirty(this)
}

fun main(args: Array<String>) {
    println(Test::bla.javaGetter)
}