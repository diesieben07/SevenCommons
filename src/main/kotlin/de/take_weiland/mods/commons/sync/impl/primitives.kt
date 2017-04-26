@file:Suppress("NOTHING_TO_INLINE")

package de.take_weiland.mods.commons.sync.impl

import de.take_weiland.mods.commons.sync.BaseSyncedProperty
import de.take_weiland.mods.commons.sync.markDirty
import net.minecraftforge.common.capabilities.ICapabilityProvider
import kotlin.reflect.KProperty

// type-aliases + overloading :D
inline fun <R : ICapabilityProvider> sync(initialValue: Boolean) = BooleanSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Byte) = ByteSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Char) = CharSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Short) = ShortSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Int) = IntSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Long) = LongSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Float) = FloatSyncedProperty<R>(initialValue)
inline fun <R : ICapabilityProvider> sync(initialValue: Double) = DoubleSyncedProperty<R>(initialValue)

class BooleanSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Boolean) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Boolean = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Boolean) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class ByteSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Byte) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Byte = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Byte) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class CharSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Char) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Char = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Char) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class ShortSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Short) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Short = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Short) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class IntSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Int) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Int = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Int) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class LongSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Long) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Long = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Long) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}

class FloatSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Float) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Float = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Float) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}

class DoubleSyncedProperty<R : ICapabilityProvider>(@JvmField var value: Double) : BaseSyncedProperty() {

    inline operator fun getValue(self: R, property: KProperty<*>): Double = value

    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Double) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}
