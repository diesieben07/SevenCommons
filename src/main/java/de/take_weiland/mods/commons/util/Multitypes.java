package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.templates.HasMetadata;
import de.take_weiland.mods.commons.templates.Metadata;
import de.take_weiland.mods.commons.templates.Metadata.BlockMeta;
import de.take_weiland.mods.commons.templates.Metadata.ItemMeta;
import net.minecraft.item.ItemStack;

public final class Multitypes {

	private Multitypes() { }
	
	public static <TYPE extends Metadata> TYPE getType(HasMetadata<TYPE> typed, int meta) {
		return JavaUtils.defaultedArrayAccess(typed.getTypes(), meta, typed.getDefault());
	}
	
	public static <TYPE extends Metadata> TYPE getType(HasMetadata<TYPE> typed, ItemStack stack) {
		return getType(typed, stack.getItemDamage());
	}
	
	public static String name(ItemMeta meta) {
		return meta.getItem().getUnlocalizedName() + "." + meta.unlocalizedName();
	}
	
	public static String name(BlockMeta type) {
		return type.getBlock().getUnlocalizedName() + "." + type.unlocalizedName();
	}

	public static String fullName(ItemMeta type) {
		return name(type) + ".name";
	}
	
	public static String fullName(BlockMeta type) {
		return name(type) + ".name";
	}
}
