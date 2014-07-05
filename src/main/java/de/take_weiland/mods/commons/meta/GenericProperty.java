package de.take_weiland.mods.commons.meta;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
abstract class GenericProperty<T> implements MetadataProperty<T> {

	@Override
	public final T value(ItemStack stack) {
		return value(stack.getItemDamage());
	}

	@Override
	public final T value(World world, int x, int y, int z) {
		return value(world.getBlockMetadata(x, y, z));
	}

	@Override
	public ItemStack apply(T value, ItemStack stack) {
		stack.setItemDamage(toMeta(value, stack.getItemDamage()));
		return stack;
	}

	@Override
	public void apply(T value, World world, int x, int y, int z) {
		world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), 3);
	}

	@Override
	public void apply(T value, World world, int x, int y, int z, int notifyFlags) {
		world.setBlockMetadataWithNotify(x, y, z, toMeta(value, world.getBlockMetadata(x, y, z)), notifyFlags);
	}
}
