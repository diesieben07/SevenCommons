package de.take_weiland.mods.commons.util

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.registry.IForgeRegistry
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry
import net.minecraftforge.fml.common.registry.PersistentRegistryManager

/**
 * @author diesieben07
 */
val <V : IForgeRegistryEntry<V>> V.registry: IForgeRegistry<V> get() = GameRegistry.findRegistry(registryType)

val IForgeRegistry<*>.registryName: ResourceLocation get() = PersistentRegistryManager.getRegistryRegistryName(this)