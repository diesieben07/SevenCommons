package de.take_weiland.mods.commons.metadata;

import de.take_weiland.mods.commons.internal.MetadataBlockProxy;
import de.take_weiland.mods.commons.internal.MetadataItemProxy;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public final class Meta {

	public static <T extends Metadata, S extends HasMetadata<T>> T get(ItemStack stack, S holder) {
		return ((MetadataItemProxy<T>) holder)._sc$getMetadata(holder, stack);
	}

	public static <T extends Metadata, S extends Block & HasMetadata<T>> T get(World world, int x, int y, int z, S holder) {
		return ((MetadataBlockProxy<T>) holder)._sc$getMetadata(holder, world, x, y, z);
	}

	private Meta() { }

}
