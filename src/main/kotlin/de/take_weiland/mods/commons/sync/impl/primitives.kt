@file:Suppress("NOTHING_TO_INLINE")

package de.take_weiland.mods.commons.sync.impl

import de.take_weiland.mods.commons.sync.BaseSyncedProperty
import de.take_weiland.mods.commons.sync.markDirty
import kotlin.reflect.KProperty

// type-aliases + overloading :D
inline fun sync(initialValue: Boolean) = BooleanSyncedProperty(initialValue)
inline fun sync(initialValue: Byte) = ByteSyncedProperty(initialValue)
inline fun sync(initialValue: Char) = CharSyncedProperty(initialValue)
inline fun sync(initialValue: Short) = ShortSyncedProperty(initialValue)
inline fun sync(initialValue: Int) = IntSyncedProperty(initialValue)
inline fun sync(initialValue: Long) = LongSyncedProperty(initialValue)
inline fun sync(initialValue: Float) = FloatSyncedProperty(initialValue)
inline fun sync(initialValue: Double) = DoubleSyncedProperty(initialValue)

class BooleanSyncedProperty(private var value: Boolean) : BaseSyncedProperty<Boolean>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): BooleanSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Boolean = value

    operator fun setValue(self: Any, property: KProperty<*>, newValue: Boolean) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class ByteSyncedProperty(private var value: Byte) : BaseSyncedProperty<Byte>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): ByteSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Byte = value

    operator fun setValue(self: Any, property: KProperty<*>, newValue: Byte) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class CharSyncedProperty(private var value: Char) : BaseSyncedProperty<Char>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): CharSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Char = value

    operator fun setValue(self: Any, property: KProperty<*>, newValue: Char) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class ShortSyncedProperty(private var value: Short) : BaseSyncedProperty<Short>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): ShortSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Short = value

    operator fun setValue(self: Any, property: KProperty<*>, newValue: Short) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class IntSyncedProperty(private var value: Int) : BaseSyncedProperty<Int>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): IntSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Int = value

    operator fun setValue(self: Any, property: KProperty<*>, newValue: Int) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

class LongSyncedProperty(private var value: Long) : BaseSyncedProperty<Long>() {
    
    override operator fun provideDelegate(obj: Any, property: KProperty<*>): LongSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Long = value
    operator fun setValue(self: Any, property: KProperty<*>, newValue: Long) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}

class FloatSyncedProperty(private var value: Float) : BaseSyncedProperty<Float>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): FloatSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Float = value
    operator fun setValue(self: Any, property: KProperty<*>, newValue: Float) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}

class DoubleSyncedProperty(private var value: Double) : BaseSyncedProperty<Double>() {

    override operator fun provideDelegate(obj: Any, property: KProperty<*>): DoubleSyncedProperty {
        super.provideDelegate(obj, property)
        return this
    }

    operator fun getValue(self: Any, property: KProperty<*>): Double = value
    operator fun setValue(self: Any, property: KProperty<*>, newValue: Double) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }

}
