package de.take_weiland.mods.commons.sync

import net.minecraftforge.common.capabilities.ICapabilityProvider
import kotlin.reflect.KProperty

abstract class BaseSyncedProperty internal constructor() {

    internal var id0: Int = -1
    internal inline fun getId(capability: SyncCapability<*>): Int = id0.let {
        if (it == -1) capability.computeId(this) else it
    }

}

abstract class SyncedProperty<T, in R> : BaseSyncedProperty() {

    abstract operator fun getValue(obj: R, property: KProperty<*>): T
    abstract operator fun setValue(obj: R, property: KProperty<*>, newValue: T)

}

private fun ICapabilityProvider.getSyncCapability() = getCapability(SyncCapHolder.SYNC_CAP_KEY, null)!!

fun <C : ICapabilityProvider> BaseSyncedProperty.markDirty(self: C) {
    self.getSyncCapability().markDirty(this)
}