package de.take_weiland.mods.commons.util;

import java.util.List;

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
		return JavaUtils.defaultedArrayAccess(typed.getTypes(), meta, typed.getDefault());
	}
	
	public static final <E extends Type<E>> E getType(Typed<E> typed, ItemStack stack) {
		return getType(typed, stack.getItemDamage());
	}
	
	public static <T extends Typed<R>, R extends Type<R>> List<ItemStack> allStacks(T typed) {
		return stacks(typed.getTypes());
	}
	
	public static <T extends Typed<R>, R extends Type<R>> List<ItemStack> stacks(R... types) {
		return ImmutableList.copyOf(Iterators.transform(Iterators.forArray(types), GET_STACK_FUNC));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated // use allStacks
	public static void addSubtypes(Typed<?> typed, List stacks) {
		for (Type type : typed.getTypes()) {
			stacks.add(type.stack());
		}
	}
	
	public static boolean is(ItemStack stack, Type<?> type) {
		return type.getTyped().isThis(stack) && type.ordinal() == stack.getItemDamage();
	}
	
	public static boolean isAny(ItemStack stack, Stackable... stackables) {
		for (Stackable s : stackables) {
			if (s.isThis(stack)) {
				return true;
			}
		}
		return false;
	}
	
	public static <T extends Type<T>, E extends Typed<T>> String name(T type) {
		return type.getTyped().subtypeName(type);
	}
	
	private static final Function<Stackable, ItemStack> GET_STACK_FUNC = new Function<Stackable, ItemStack>() {

		@Override
		public ItemStack apply(Stackable input) {
			return input.stack();
		}
	};
	
	public static Function<Stackable, ItemStack> getStackFunction() {
		return GET_STACK_FUNC;
	}

	static <T extends Type<T>> void registerSubtypes(Typed<T> typed, String baseName) {
		for (T type : typed.getTypes()) {
			GameRegistry.registerCustomItemStack(name(type), type.stack());
		}
	}

	public static ItemStack stack(Type<?> type, int quantity) {
		ItemStack stack = type.getTyped().stack(quantity);
		stack.setItemDamage(type.ordinal());
		return stack;
	}
	
}
