package de.take_weiland.mods.commons.sync

import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KProperty

interface CapturedSync<C : Any> {
}
object TileEntityCapturedSync : CapturedSync<TileEntity>
object EntityCapturedSync : CapturedSync<Entity>
object ContainerCapturedSync : CapturedSync<Container>

val <T : TileEntity> T.sync inline get() = TileEntityCapturedSync
val <T : Entity> T.sync inline get() = EntityCapturedSync
val <T : Container> T.sync inline get() = ContainerCapturedSync

fun <CONTAINER : Any> sync(initialValue: Int): IntSyncedProperty<CONTAINER> {
    return IntSyncedProperty(initialValue)
}

inline operator fun <T : TileEntity, R> ((T, KProperty<*>, SyncedContainerType<in T, *>) -> R).provideDelegate(obj: T, property: KProperty<*>): R {
    return this(obj, property, TileEntitySyncedType)
}

inline operator fun <T : Entity, R>   ((T, KProperty<*>, SyncedContainerType<in T, *>) -> R).provideDelegate(obj: T, property: KProperty<*>): R {
    return this(obj, property, EntitySyncedType)
}

inline operator fun <T : Container, R> ((T, KProperty<*>, SyncedContainerType<in T, *>) -> R).provideDelegate(obj: T, property: KProperty<*>): R {
    return this(obj, property, ContainerSyncedType)
}

class IntSyncedProperty<in CONTAINER : Any>(@JvmField var value: Int) : SyncedProperty() {

    inline operator fun getValue(obj: CONTAINER, property: KProperty<*>): Int {
        return value
    }

    inline operator fun setValue(obj: CONTAINER, property: KProperty<*>, newValue: Int) {
        if (value != newValue) {
            value = newValue
            markDirty(obj, newValue)
        }
    }

}