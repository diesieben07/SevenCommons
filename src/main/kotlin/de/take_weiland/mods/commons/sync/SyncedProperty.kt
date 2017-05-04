package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.util.fastComputeIfAbsent
import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

abstract class SyncedProperty<CONTAINER, DATA>(private val obj: CONTAINER) {

    var id: Int = -1

    internal lateinit var containerType: SyncedContainerType<CONTAINER>

    internal val world: World
        get() = containerType.getWorld(obj)

    fun init(property: KProperty<*>, containerType: SyncedContainerType<CONTAINER>) {
        this.id = (property as? KProperty1<*, *> ?: throw UnsupportedOperationException("Only member properties in a class can be synced.")).getPropertyId()
        this.containerType = containerType
    }

    abstract fun write(out: MCDataOutput): DATA

}

operator fun <C : TileEntity, P : SyncedProperty<C, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
    init(property, TileEntitySyncedType)
    return this
}

operator fun <C : Entity, P : SyncedProperty<C, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
    init(property, EntitySyncedType)
    return this
}

operator fun <C : Container, P : SyncedProperty<C, *>> P.provideDelegate(obj: C, property: KProperty<*>): P {
    init(property, ContainerSyncedType)
    return this
}

interface TickingProperty {

    fun update()

}

class SyncedPropertyImmutable<C, T>(obj: C, @JvmField var value: T) : SyncedProperty<C, T>(obj) {

    inline operator fun getValue(obj: C, property: KProperty<*>): T = value

    inline operator fun setValue(obj: C, property: KProperty<*>, newValue: T) {
        if (value != newValue) {
            value = newValue
            markDirty(obj, newValue)
        }
    }

    override fun write(out: MCDataOutput): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class SyncedPropertyIdentityImmutable<C, T>(obj: C, @JvmField var value: T) : SyncedProperty<C, T>(obj) {

    inline operator fun getValue(obj: C, property: KProperty<*>): T = value

    inline operator fun setValue(obj: C, property: KProperty<*>, newValue: T) {
        if (value !== newValue) {
            value = newValue
            markDirty(obj, newValue)
        }
    }

    override fun write(out: MCDataOutput): T {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

abstract class SyncedPropertyMutable<C, T>(@JvmField var value: T, obj: C) : SyncedProperty<C, T>(obj), TickingProperty {

    @JvmField
    var oldValue = value

    inline operator fun getValue(obj: C, property: KProperty<*>): T = value

    inline operator fun setValue(obj: C, property: KProperty<*>, newValue: T) {
        value = newValue
    }

    override fun update() {
        value.let {
            if (it != oldValue) {
                oldValue = value.copy()
//                markDirty()
            }
        }
    }

    protected abstract fun T.copy() : T

}

internal val dirtyProperties = HashMap<Any?, ChangedPropertyStore<Any?>>()

fun <CONTAINER, DATA> SyncedProperty<CONTAINER,DATA>.markDirty(obj: CONTAINER, data: DATA) {
    dirtyProperties.fastComputeIfAbsent(obj) { ChangedPropertyStore() }.put(id, data)
}