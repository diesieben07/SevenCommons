package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.sync.SyncCapHolder.SYNC_CAP_KEY
import de.take_weiland.mods.commons.util.immutableSetOf
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

internal class SyncCapability<R : ICapabilityProvider>(private val obj : R) : ICapabilityProvider {

    val dirtyProperties = hashSetOf<BaseSyncedProperty>()

    fun computeId(property: BaseSyncedProperty): Int {
        val (containingClass, kotlinProperty) = SyncedPropertyJavaHelper.getPropertyInfo(property, obj.javaClass, obj)
        val propId = SyncedPropertyJavaHelper.getPropertyId(kotlinProperty, containingClass, obj)
        return (propId shl CLASS_ID_BITS) or containingClass.id
    }

    fun markDirty(property: BaseSyncedProperty) {
        dirtyProperties += property
        println("property $property is now dirty")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? {
        return if (capability === SYNC_CAP_KEY) this as T else null
    }

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?): Boolean = capability === SYNC_CAP_KEY
}

private const val CLASS_ID_BITS = 3
private const val MAX_CLASS_ID = (1 shl (CLASS_ID_BITS + 1)) - 1
private val classHierarchyIgnored = immutableSetOf(Object::class.java, TileEntity::class.java)

private val KClass<*>.id: Int
    get() {
        return (this.java.hierarchy()
                .filter { it !in classHierarchyIgnored }
                .count() - 1)
                .takeIf { it in 0..MAX_CLASS_ID }
                .let { it ?: throw UnsupportedOperationException("Class hierarchy too deep for synced properties") }
    }

private fun <T> Class<T>.hierarchy() = generateSequence<Class<in T>>(this) { it.superclass }

// ID is built as follows:
//      - Lowest 3 bits are class ID, starting with 0 at the top. Top is the class extending from e.g. TileEntity.
//        Classes without synced properties are still counted
//        This allows for a 7-deep hierarchy, which should be plenty
//      - Rest is property id, in order of definition in the source file.
// This allows classes to add synced properties without changing other classes' IDs
// The ID will fit in one VarInt byte so long as there are no more than 16 synced properties in a class
// Longer IDs appear on a per-class basis, so only classes with more than 16 properties get longer IDs.

private val idCache = ConcurrentHashMap<KProperty<*>, Int>()

private fun <T : Any> KProperty1<T, *>.getId(clazz: KClass<T>, obj: T): Int {
    return idCache.computeIfAbsent(this, {
        val propId = SyncedPropertyJavaHelper.getPropertyId(this, clazz, obj)
        (propId shl CLASS_ID_BITS) or clazz.id
    })
}