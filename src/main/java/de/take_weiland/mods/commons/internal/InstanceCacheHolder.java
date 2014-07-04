package de.take_weiland.mods.commons.internal;

import com.google.common.base.Function;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public final class InstanceCacheHolder {

	public static Function<Block, ItemStack> BLOCK_STACK_FUNCTION = new Function<Block, ItemStack>() {
		@Override
		public ItemStack apply(Block input) {
			return new ItemStack(input);
		}
	};
	public static Function<Item, ItemStack> ITEM_STACK_FUNCTION = new Function<Item, ItemStack>() {
		@Override
		public ItemStack apply(Item item) {
			return new ItemStack(item);
		}
	};

	public static void init() {
		BLOCK_STACK_FUNCTION = null;
		ITEM_STACK_FUNCTION = null;
	}

}
