package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.sync.SyncCapHolder.SYNC_CAP_KEY
import de.take_weiland.mods.commons.util.immutableSetOf
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import kotlin.reflect.KClass

internal class SyncCapability<R : ICapabilityProvider>(private val obj : R) : ICapabilityProvider {

    val dirtyProperties = hashSetOf<BaseSyncedProperty>()

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

internal const val CLASS_ID_BITS = 3
private const val MAX_CLASS_ID = (1 shl (CLASS_ID_BITS + 1)) - 1
private val classHierarchyIgnored = immutableSetOf(Object::class.java, TileEntity::class.java)

val KClass<*>.id: Int
    get() {
        return (this.java.hierarchy()
                .filter { it !in classHierarchyIgnored }
                .count() - 1)
                .takeIf { it in 0..MAX_CLASS_ID }
                .let { it ?: throw UnsupportedOperationException("Class hierarchy too deep for synced properties") }
    }

private fun <T> Class<T>.hierarchy() = generateSequence<Class<in T>>(this) { it.superclass }

