package de.take_weiland.mods.commons.util

import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilityProvider
import java.util.*

private val allCaps = run {
    val field = CapabilityManager::class.java.getDeclaredField("providers")
    field.isAccessible = true
    @Suppress("UNCHECKED_CAST")
    field.get(CapabilityManager.INSTANCE) as IdentityHashMap<String, Capability<*>>
}

@PublishedApi
internal object ClassToCap : ClassValue<Capability<*>>() {
    override fun computeValue(type: Class<*>): Capability<*> {
        return allCaps[type.name.intern()] ?: throw IllegalArgumentException("Capability interface ${type.name} not known.")
    }
}

inline fun <reified T> ICapabilityProvider.getCapability(side: EnumFacing?): T? {
    @Suppress("UNCHECKED_CAST")
    return getCapability(ClassToCap.get(T::class.java) as Capability<T>, side)
}

inline fun <reified T> ICapabilityProvider.getCapability(): T? {
    return getCapability(null)
}