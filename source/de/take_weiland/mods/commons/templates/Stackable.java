package de.take_weiland.mods.commons.templates;

import net.minecraft.item.ItemStack;

public interface Stackable {

	ItemStack stack();
	
	ItemStack stack(int quantity);
	
	boolean isThis(ItemStack stack);
	
}
