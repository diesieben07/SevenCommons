package de.take_weiland.mods.commons.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * <p>A basic implementation of {@link Slot} that delegates {@link Slot#isItemValid(ItemStack)} to
 * {@link IInventory#isItemValidForSlot(int, ItemStack)}.</p>
 */
public class SimpleSlot extends Slot {

    public SimpleSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(slotNumber, stack);
    }

}
