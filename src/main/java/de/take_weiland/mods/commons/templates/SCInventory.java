package de.take_weiland.mods.commons.templates;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import de.take_weiland.mods.commons.util.Listenable;

public interface SCInventory<T extends SCInventory<T>> extends IInventory, Listenable<T> {

	ItemStack[] getItemStorage();
	
	void onChange();
	
}
