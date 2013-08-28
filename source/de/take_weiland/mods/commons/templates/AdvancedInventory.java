package de.take_weiland.mods.commons.templates;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface AdvancedInventory extends IInventory {

	ItemStack[] getStorage();
	
}
