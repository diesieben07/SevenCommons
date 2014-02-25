package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.Listenable;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface SCInventory<T extends SCInventory<T>> extends IInventory, Listenable<T> {

	ItemStack[] getItemStorage();
	
}
