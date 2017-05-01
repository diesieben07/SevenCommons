package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.util.isServer
import gnu.trove.set.hash.THashSet
import net.minecraft.entity.Entity
import net.minecraft.inventory.Container
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaGetter

abstract class SyncedProperty<out R : Any>(private val obj: R) {

    private var id: Int = -1

    private lateinit var containerType: SyncedContainerType<R>

    internal val world: World
        get() = containerType.getWorld(obj)

    fun init(property: KProperty<*>, containerType: SyncedContainerType<R>) {
        this.id = (property as? KProperty1<*, *> ?: throw UnsupportedOperationException("Only member properties in a class can be synced.")).getPropertyId()
        this.containerType = containerType
    }

}

operator fun <R : TileEntity, T : SyncedProperty<R>> T.provideDelegate(obj: R, property: KProperty<*>): T {
    init(property, TileEntitySyncedType)
    return this
}

operator fun <R : Entity, T : SyncedProperty<R>> T.provideDelegate(obj: R, property: KProperty<*>): T {
    init(property, EntitySyncedType)
    return this
}

operator fun <R : Container, T : SyncedProperty<R>> T.provideDelegate(obj: R, property: KProperty<*>): T {
    init(property, ContainerSyncedType)
    return this
}

interface TickingProperty {

    fun update()

}

class SyncedPropertyImmutable<T, R : Any>(@JvmField var value: T, obj: R) : SyncedProperty<R>(obj) {

    inline operator fun getValue(obj: R, property: KProperty<*>): T = value

    inline operator fun setValue(obj: R, property: KProperty<*>, newValue: T) {
        if (value != newValue) {
            value = newValue
            markDirty()
        }
    }

}

class SyncedPropertyIdentityImmutable<T, R : Any>(@JvmField var value: T, obj: R) : SyncedProperty<R>(obj) {

    inline operator fun getValue(obj: R, property: KProperty<*>): T = value

    inline operator fun setValue(obj: R, property: KProperty<*>, newValue: T) {
        if (value !== newValue) {
            value = newValue
            markDirty()
        }
    }

}

abstract class SyncedPropertyMutable<T, R : Any>(@JvmField var value: T, obj: R) : SyncedProperty<R>(obj), TickingProperty {

    @JvmField
    var oldValue = value

    inline operator fun getValue(obj: R, property: KProperty<*>): T = value

    inline operator fun setValue(obj: R, property: KProperty<*>, newValue: T) {
        value = newValue
    }

    override fun update() {
        value.let {
            if (it != oldValue) {
                oldValue = value.copy()
                markDirty()
            }
        }
    }

    protected abstract fun T.copy() : T

}

val dirtyProperties = WeakHashMap<World, MutableSet<SyncedProperty<*>>>()

fun SyncedProperty<*>.markDirty() {
    world.let { world ->
        if (world.isServer) {
            (dirtyProperties[world] ?: (THashSet<SyncedProperty<*>>().also { dirtyProperties[world] = it })) += this
        }
    }
}

fun main(args: Array<String>) {
    println(Test::bla.javaGetter)
}