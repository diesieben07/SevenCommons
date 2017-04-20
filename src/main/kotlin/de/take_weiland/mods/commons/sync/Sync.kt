package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KProperty

interface SyncedProperty<T, in C> {

    operator fun getValue(self: C, property: KProperty<*>): T
    operator fun setValue(self: C, property: KProperty<*>, newValue: T)

}

interface TickingSyncedProperty<T, in C> : SyncedProperty<T, C> {

    fun update(self: C)

}

fun <T, C> SyncedProperty<T, C>.markDirty(self: C) {

}

class ImmutableObjectSyncedProperty<T, in C>(var value: T) : SyncedProperty<T, C> {

    override fun getValue(self: C, property: KProperty<*>): T = value

    override fun setValue(self: C, property: KProperty<*>, newValue: T) {
        if (value != newValue) {
            value = newValue
            markDirty(self)
        }
    }
}

abstract class MutableObjectSyncedPropert<T, in C>(var value: T) : TickingSyncedProperty<T, C> {

    private abstract var lastValue: T

    override fun getValue(self: C, property: KProperty<*>): T = value

    override fun setValue(self: C, property: KProperty<*>, newValue: T) {
        value = newValue
    }

    override fun update(self: C) {
        if ()
    }

    abstract fun clone(t: T): T
}

fun <T> sync(): SyncedProperty<T> = TODO()
fun <T> sync(initialValue: T): SyncedProperty<T> = TODO()
