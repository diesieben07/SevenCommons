//@file:Suppress("NOTHING_TO_INLINE")
//
//package de.take_weiland.mods.commons.sync.impl
//
//import de.take_weiland.mods.commons.sync.SyncedProperty
//import de.take_weiland.mods.commons.sync.markDirty
//import sun.awt.image.SunWritableRaster.markDirty
//import kotlin.reflect.KProperty
//
//// type-aliases + overloading :D
//inline fun <R : Any> R.sync(initialValue: Boolean) = BooleanSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Byte) = ByteSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Char) = CharSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Short) = ShortSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Int) = IntSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Long) = LongSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Float) = FloatSyncedProperty(initialValue, this)
//inline fun <R : Any> R.sync(initialValue: Double) = DoubleSyncedProperty(initialValue, this)
//
//class BooleanSyncedProperty<R : Any>(@JvmField var value: Boolean, obj: R) : SyncedProperty<R, Boolean, Boolean>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Boolean = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Boolean) {
//        if (value != newValue) {
//            value = newValue
//            markDirty(self, newValue)
//        }
//    }
//
//    override fun read(data: Boolean) {
//        property.set(obj, data)
//    }
//
//
//}
//
//class ByteSyncedProperty<R : Any>(@JvmField var value: Byte, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Byte = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Byte) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//}
//
//class CharSyncedProperty<R : Any>(@JvmField var value: Char, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Char = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Char) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//}
//
//class ShortSyncedProperty<R : Any>(@JvmField var value: Short, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Short = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Short) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//}
//
//class IntSyncedProperty<R : Any>(@JvmField var value: Int, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Int = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Int) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//}
//
//class LongSyncedProperty<R : Any>(@JvmField var value: Long, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Long = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Long) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//
//}
//
//class FloatSyncedProperty<R : Any>(@JvmField var value: Float, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Float = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Float) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//
//}
//
//class DoubleSyncedProperty<R : Any>(@JvmField var value: Double, obj: R) : SyncedProperty<R>(obj) {
//
//    inline operator fun getValue(self: R, property: KProperty<*>): Double = value
//
//    inline operator fun setValue(self: R, property: KProperty<*>, newValue: Double) {
//        if (value != newValue) {
//            value = newValue
//            markDirty()
//        }
//    }
//
//}
