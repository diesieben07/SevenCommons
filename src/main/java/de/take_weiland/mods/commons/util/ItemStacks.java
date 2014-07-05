package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemStacks {

	private ItemStacks() { }

	@Deprecated
	public static boolean canMergeFully(ItemStack from, ItemStack into) {
		return fitsInto(from, into);
	}

	public static boolean fitsInto(ItemStack from, ItemStack into) {
		return from == null || into == null || canMergeFullyImpl(from, into);
	}
	
	private static boolean canMergeFullyImpl(ItemStack from, ItemStack into) {
		return equalsImpl(from, into) && from.stackSize + into.stackSize <= into.getMaxStackSize();
	}
	
	@Deprecated()
	public static boolean containsSame(ItemStack a, ItemStack b) {
		return equal(a, b);
	}
	
	public static boolean equal(ItemStack a, ItemStack b) {
		return a == b || !(a == null ^ b == null) && equalsImpl(a, b);
	}
	
	private static boolean equalsImpl(ItemStack stack1, ItemStack stack2) {
		return stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}
	
	public static ItemStack merge(ItemStack from, ItemStack into) {
		return merge(from, into, false);
	}
	
	public static ItemStack merge(ItemStack from, ItemStack into, boolean force) {
		if (from == null) {
			return into;
		}
		
		if (into == null) {
			ItemStack result = from.copy();
			from.stackSize = 0;
			return result;
		}
		
		if (force || equalsImpl(from, into)) {
			int transferCount = Math.min(into.getMaxStackSize() - into.stackSize, from.stackSize);
			from.stackSize -= transferCount;
			into.stackSize += transferCount;
		}
		return into;
	}
	
	public static ItemStack emptyToNull(ItemStack stack) {
		return stack == null || stack.stackSize <= 0 ? null : stack;
	}

	public static NBTTagCompound getNbt(ItemStack stack) {
		if (stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}
		return stack.stackTagCompound;
	}
	
	public static ItemStack of(Item item) {
		return new ItemStack(item);
	}
	
	public static ItemStack of(Item item, int quantity) {
		return new ItemStack(item, quantity);
	}
	
	public static ItemStack of(Item item, int quantity, int meta) {
		return new ItemStack(item, quantity, meta);
	}
	
	public static ItemStack of(Block block) {
		return new ItemStack(block);
	}
	
	public static ItemStack of(Block block, int quantity) {
		return new ItemStack(block, quantity);
	}
	
	public static ItemStack of(Block block, int quantity, int meta) {
		return new ItemStack(block, quantity, meta);
	}

	public static boolean is(ItemStack stack, int id) {
		return stack != null && stack.itemID == id;
	}
	
	public static boolean is(ItemStack stack, int id, int meta) {
		return stack != null && stack.itemID == id && stack.getItemDamage() == meta;
	}
	
	public static boolean is(ItemStack stack, Item item) {
		return stack != null && stack.itemID == item.itemID;
	}
	
	public static boolean is(ItemStack stack, Item item, int meta) {
		return stack != null && stack.itemID == item.itemID && stack.getItemDamage() == meta;
	}
	
	public static boolean is(ItemStack stack, Block block, int meta) {
		return stack != null && stack.itemID == block.blockID && stack.getItemDamage() == meta;
	}
	
	public static boolean is(ItemStack stack, Block block) {
		return stack != null && stack.itemID == block.blockID;
	}

	@SuppressWarnings("unchecked")
	static <T extends Enum<T> & Subtype, R> void registerSubstacks(String baseName, R item, Function<R, ItemStack> function) {
		MetadataProperty<T> prop = ((HasSubtypes<T>) item).subtypeProperty();
		T[] types = prop.values();
		for (T type : types) {
			ItemStack stack = function.apply(item);
			stack.setItemDamage(prop.toMeta(type, 0));

			String name = baseName + "." + type.subtypeName();
			GameRegistry.registerCustomItemStack(name, stack);
		}
	}
}
