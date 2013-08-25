package de.take_weiland.mods.commons.util;

import net.minecraft.item.ItemStack;

public final class ItemStacks {

	private ItemStacks() { }
	
	public static final boolean canMergeFully(ItemStack from, ItemStack into) {
		return from == null || into == null || canMergeFullyImpl(from, into);
	}
	
	private static final boolean canMergeFullyImpl(ItemStack from, ItemStack into) {
		return containsSameImpl(from, into) && from.stackSize + into.stackSize <= from.getMaxStackSize();
	}
	
	public static final boolean containsSame(ItemStack stack1, ItemStack stack2) {
		if (stack1 == stack2) {
			return true;
		}
		if (stack1 == null ^ stack2 == null) {
			return false;
		}
		return containsSameImpl(stack1, stack2);
	}
	
	private static final boolean containsSameImpl(ItemStack stack1, ItemStack stack2) {
		return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
	
	public static final ItemStack merge(ItemStack from, ItemStack into) {
		return merge(from, into, false);
	}
	
	public static final ItemStack merge(ItemStack from, ItemStack into, boolean force) {
		if (from == null) {
			return into;
		}
		
		if (into == null) {
			ItemStack result = from.copy();
			from.stackSize = 0;
			return result;
		}
		
		if (force || containsSameImpl(from, into)) {
			int transferCount = Math.min(into.getMaxStackSize() - into.stackSize, from.stackSize);
			from.stackSize -= transferCount;
			into.stackSize += transferCount;
		}
		return into;
	}
	
	public static final ItemStack emptyToNull(ItemStack stack) {
		return stack == null || stack.stackSize <= 0 ? null : stack;
	}

}
