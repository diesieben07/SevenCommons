package de.take_weiland.mods.commons.util

import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import net.minecraftforge.registries.RegistryManager

/**
 * @author diesieben07
 */
val <V : IForgeRegistryEntry<V>> V.registry: IForgeRegistry<V> get() = GameRegistry.findRegistry(registryType)

val <V : IForgeRegistryEntry<V>> IForgeRegistry<V>.registryName: ResourceLocation get() = RegistryManager.ACTIVE.getName(this)