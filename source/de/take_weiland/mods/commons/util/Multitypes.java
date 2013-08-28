package de.take_weiland.mods.commons.util;

import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;

public final class Multitypes {

	private Multitypes() { }

	public static final <E extends Type> E getType(Typed<E> item, int meta) {
		return CollectionUtils.defaultedArrayAccess(item.getTypes(), meta, item.getDefault());
	}

	public static final <E extends Type> E getType(Typed<E> item, ItemStack stack) {
		return getType(item, stack.getItemDamage());
	}

}
