package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.templates.Type;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public abstract class MultiTypeManager<T extends Enum<T> & Type> {

	public static final <T extends Enum<T> & Type> MultiTypeManager<T> create(Item item, Class<T> types, T defaultValue) {
		return new MultiTypeManager.ForItem<T>(item, types.getEnumConstants(), defaultValue);
	}
	
	public static final <T extends Enum<T> & Type> MultiTypeManager<T> create(Block block, Class<T> types, T defaultValue) {
		return new MultiTypeManager.ForBlock<T>(block, types.getEnumConstants(), defaultValue);
	}
	
	public final ItemStack getStack(T type) {
		return getStack(type.ordinal(), 1);
	}
	
	public final ItemStack getStack(T type, int quantity) {
		return getStack(type.ordinal(), quantity);
	}
	
	public final ItemStack getStack(int meta) {
		return getStack(meta, 1);
	}
	
	public final ItemStack getUniversalStack() {
		return getStack(OreDictionary.WILDCARD_VALUE, 1);
	}
	
	public final ItemStack getUniversalStack(int quantity) {
		return getStack(OreDictionary.WILDCARD_VALUE, quantity);
	}
	
	public final T getType(ItemStack item) {
		return getType(item.getItemDamage());
	}
	
	public final T getType(int meta) {
		return CommonUtils.defaultedArrayAccess(types, meta, defaultValue);
	}
	
	public final T[] getTypes() {
		return types;
	}
	
	public abstract ItemStack getStack(int meta, int quantity);
	
	private final T[] types;
	private final T defaultValue;
	
	MultiTypeManager(T[] types, T defaultValue) {
		this.types = types;
		this.defaultValue = defaultValue;
	}
	
	private static class ForItem<T extends Enum<T> & Type> extends MultiTypeManager<T> {

		private final Item item;
		
		ForItem(Item item, T[] types, T defaultValue) {
			super(types, defaultValue);
			this.item = item;
		}

		@Override
		public ItemStack getStack(int meta, int quantity) {
			return new ItemStack(item, quantity, meta);
		}
		
	}
	
	private static class ForBlock<T extends Enum<T> & Type> extends MultiTypeManager<T> {

		private final Block block;
		
		ForBlock(Block block, T[] types, T defaultValue) {
			super(types, defaultValue);
			this.block = block;
		}

		@Override
		public ItemStack getStack(int meta, int quantity) {
			return new ItemStack(block, quantity, meta);
		}
		
	}
	
}
