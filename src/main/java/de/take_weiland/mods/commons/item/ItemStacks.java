package de.take_weiland.mods.commons.item;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.templates.HasMetadata;
import de.take_weiland.mods.commons.templates.Metadata;
import de.take_weiland.mods.commons.templates.Metadata.BlockMeta;
import de.take_weiland.mods.commons.templates.Metadata.ItemMeta;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

public final class ItemStacks {

	static final Function<ItemMeta, ItemStack> ITEM_GET_STACK = new Function<ItemMeta, ItemStack>() {
	
		@Override
		public ItemStack apply(ItemMeta type) {
			return of(type);
		}
		
	};
	public static final Function<BlockMeta, ItemStack> BLOCK_GET_STACK = new Function<BlockMeta, ItemStack>() {

		@Override
		public ItemStack apply(BlockMeta type) {
			return of(type);
		}
		
	};

	private ItemStacks() { }

	public static boolean canMergeFully(ItemStack from, ItemStack into) {
		return from == null || into == null || canMergeFullyImpl(from, into);
	}
	
	private static boolean canMergeFullyImpl(ItemStack from, ItemStack into) {
		return containsSameImpl(from, into) && from.stackSize + into.stackSize <= into.getMaxStackSize();
	}
	
	@Deprecated()
	public static boolean containsSame(ItemStack a, ItemStack b) {
		return equal(a, b);
	}
	
	public static boolean equal(ItemStack a, ItemStack b) {
		return a == b || !(a == null ^ b == null) && containsSameImpl(a, b);
	}
	
	private static boolean containsSameImpl(ItemStack stack1, ItemStack stack2) {
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
		
		if (force || containsSameImpl(from, into)) {
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

	public static ItemStack of(ItemMeta meta) {
		return new ItemStack(meta.getItem(), 1, meta.ordinal());
	}
	
	public static ItemStack of(ItemMeta meta, int quantity) {
		return new ItemStack(meta.getItem(), quantity, meta.ordinal());
	}
	
	public static ItemStack of(BlockMeta meta) {
		return new ItemStack(meta.getBlock(), 1, meta.ordinal());
	}
	
	public static ItemStack of(BlockMeta meta, int quantity) {
		return new ItemStack(meta.getBlock(), quantity, meta.ordinal());
	}
	
	public static List<ItemStack> of(ItemMeta... metas) {
		return Lists.transform(Arrays.asList(metas), ITEM_GET_STACK);
	}
	
	public static List<ItemStack> of(BlockMeta... metas) {
		return Lists.transform(Arrays.asList(metas), BLOCK_GET_STACK);
	}
	
	public static <T extends Block & HasMetadata<? extends BlockMeta>> List<ItemStack> allOf(T typed) {
		return of(typed.getTypes());
	}
	
	public static <T extends Item & HasMetadata<? extends ItemMeta>> List<ItemStack> allOf(T typed) {
		return of(typed.getTypes());
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
	
	public static boolean is(ItemStack stack, ItemMeta meta) {
		return is(stack, meta.getItem().itemID, meta.ordinal());
	}
	
	public static boolean is(ItemStack stack, BlockMeta meta) {
		return is(stack, meta.getBlock().blockID, meta.ordinal());
	}
	
	public static boolean isAny(ItemStack stack, ItemMeta... metas) {
		for (ItemMeta meta : metas) {
			if (is(stack, meta)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAny(ItemStack stack, BlockMeta... metas) {
		for (BlockMeta meta : metas) {
			if (is(stack, meta)) {
				return true;
			}
		}
		return false;
	}
	
	public static <TYPE extends Metadata> void registerAll(TYPE[] types, String baseName, Function<? super TYPE, ItemStack> stackFunction) {
		for (TYPE type : types) {
			GameRegistry.registerCustomItemStack(baseName + "." + type.unlocalizedName(), stackFunction.apply(type));
		}
	}
}
