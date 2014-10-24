package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class SlotNoPickup extends BasicSlot {

	public SlotNoPickup(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return false;
	}
}
