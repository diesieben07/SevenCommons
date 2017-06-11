package de.take_weiland.mods.commons.sync

import net.minecraft.tileentity.TileEntity

class CapturedSync<in T : Any, DATA>(val containerType: SyncedContainerType<in T, DATA>)

fun <T : TileEntity> T.sync(): TileEntitySyncedType {
    return TileEntitySyncedType
}

operator fun <T, DATA> SyncedContainerType<T,DATA>.invoke(initialValue: Int): SyncedProperty<T, Int> {
    TODO()

}