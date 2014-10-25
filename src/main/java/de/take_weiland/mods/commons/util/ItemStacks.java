package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Utilities for ItemStacks.</p>
 *
 * @see net.minecraft.item.ItemStack
 */
public final class ItemStacks {

	public static ItemStack clone(@Nullable ItemStack stack) {
		return stack == null ? null : stack.copy();
	}

	/**
	 * <p>Tests if the first ItemStack can be fully merged into the second one.</p>
	 *
	 * @param from the ItemStack to merge, may be null
	 * @param into the ItemStack to merge into, may be null
	 * @return true if the first ItemStack can be fully merged into the second one
	 */
	public static boolean fitsInto(ItemStack from, ItemStack into) {
		return from == null || into == null || fitsIntoImpl(from, into);
	}

	private static boolean fitsIntoImpl(@NotNull ItemStack from, @NotNull ItemStack into) {
		return equalsImpl(from, into) && from.stackSize + into.stackSize <= into.getMaxStackSize();
	}

	/**
	 * <p>Determine if the given ItemStacks are equal.</p>
	 * <p>This method checks the ItemID, damage value and NBT data of the stack, it does not check stack sizes.</p>
	 *
	 * @param a an ItemStack
	 * @param b an ItemStack
	 * @return true if the ItemStack are equal
	 */
	@Contract("null, null -> true; null, !null -> false; !null, null -> false")
	public static boolean equal(@Nullable ItemStack a, @Nullable ItemStack b) {
		return a == b || (a != null && b != null && equalsImpl(a, b));
	}

	private static boolean equalsImpl(@NotNull ItemStack a, @NotNull ItemStack b) {
		return a.itemID == b.itemID && a.getItemDamage() == b.getItemDamage()
				&& Objects.equal(a.stackTagCompound, b.stackTagCompound);
	}

	public static boolean identical(ItemStack a, ItemStack b) {
		return a == b || (a != null && b != null && equalsImpl(a, b) && a.stackSize == b.stackSize);
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

	public static Block getBlock(ItemStack stack) {
		return Block.blocksList[stack.itemID];
	}

	@Contract("null -> null")
	public static ItemStack emptyToNull(ItemStack stack) {
		return stack == null || stack.stackSize <= 0 ? null : stack;
	}

	public static NBTTagCompound getNbt(ItemStack stack) {
		if (stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}
		return stack.stackTagCompound;
	}

	public static NBTTagCompound getNbt(ItemStack stack, String key) {
		return NBT.getOrCreateCompound(getNbt(stack), key);
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

	private ItemStacks() {
	}
}
