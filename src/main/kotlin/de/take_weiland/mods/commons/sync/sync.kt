package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KProperty

object SyncStartHolder

class CapturedSync<T : Any, C_DATA>(val obj: T, val container: SyncedContainerType<in T, C_DATA>)

inline fun <T : TileEntity, R> T.sync(body: CapturedSync<T, *>.() -> R): R {
    return CapturedSync(this, TileEntitySyncedType).body()
}

inline fun <T : TileEntity, R> T.sync(body: SyncStartHolder.() -> )

fun <T : Any, C_DATA> CapturedSync<T, C_DATA>.int(initialValue: Int): SyncedProperty<T, Int, C_DATA> {
    return IntSyncedProperty(initialValue)
}

class IntSyncedProperty<CONTAINER : Any, C_DATA>(internal var value: Int) : SyncedProperty<CONTAINER, Int, C_DATA>() {

    operator fun getValue(obj: CONTAINER, property: KProperty<*>): Int {
        return value
    }

    operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: Int) {
        value = newValue
    }

}