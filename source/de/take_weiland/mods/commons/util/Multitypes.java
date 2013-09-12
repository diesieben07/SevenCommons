package de.take_weiland.mods.commons.util;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.templates.Stackable;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;

public final class Multitypes {

	private Multitypes() { }

	public static final <E extends Type<E>> E getType(Typed<E> typed, int meta) {
		return CollectionUtils.defaultedArrayAccess(typed.getTypes(), meta, typed.getDefault());
	}

	public static final <E extends Type<E>> E getType(Typed<E> typed, ItemStack stack) {
		return getType(typed, stack.getItemDamage());
	}
	
	public static Iterator<ItemStack> allStacksLazy(Typed<?> typed) {
		return Iterators.transform(Iterators.forArray(typed.getTypes()), GET_STACK_FUNC);
	}
	
	public static <T extends Item & Typed<R>, R extends Type<R>> List<ItemStack> allStacks(T typed) {
		return ImmutableList.copyOf(allStacksLazy(typed));
	}
	public static <T extends Block & Typed<R>, R extends Type<R>> List<ItemStack> allStacks(T typed) {
		return ImmutableList.copyOf(allStacksLazy(typed));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated // use allStacks
	public static void addSubtypes(Typed<?> typed, List stacks) {
		for (Type type : typed.getTypes()) {
			stacks.add(type.stack());
		}
	}
	
	private static final Function<Stackable, ItemStack> GET_STACK_FUNC = new Function<Stackable, ItemStack>() {

		@Override
		public ItemStack apply(Stackable input) {
			return input.stack();
		}
	};

	static void registerSubtypes(Typed<?> typed, String baseName) {
		for (Type<?> type : typed.getTypes()) {
			GameRegistry.registerCustomItemStack(Names.combine(baseName, type), type.stack());
		}
	}

	public static ItemStack stack(Type<?> type, int quantity) {
		return type.getTyped().stack(quantity, type.ordinal());
	}
	
}
