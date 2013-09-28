package de.take_weiland.mods.commons.templates;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class AdvancedSlot extends Slot {

	public AdvancedSlot(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack item) {
		return inventory.isItemValidForSlot(slotNumber, item);
	}

}
