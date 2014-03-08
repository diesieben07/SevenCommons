package de.take_weiland.mods.commons.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SimpleSlot extends Slot {

	public SimpleSlot(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack item) {
		return inventory.isItemValidForSlot(slotNumber, item);
	}

}
