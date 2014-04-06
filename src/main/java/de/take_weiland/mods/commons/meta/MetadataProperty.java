package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public interface MetadataProperty<T> {

	T value(ItemStack stack);

	T value(World world, int x, int y, int z);

	T value(int metadata);

	int toMeta(T value, int previousMeta);
}
