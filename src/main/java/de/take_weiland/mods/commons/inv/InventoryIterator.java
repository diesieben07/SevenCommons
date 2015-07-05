package de.take_weiland.mods.commons.inv;

import com.google.common.collect.UnmodifiableIterator;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.NoSuchElementException;

/**
 * @author diesieben07
 */
final class InventoryIterator extends UnmodifiableIterator<ItemStack> {

    private final IInventory inv;
    private int next;

    InventoryIterator(IInventory inv) {
        this.inv = inv;
    }

    @Override
    public boolean hasNext() {
        return next < inv.getSizeInventory();
    }

    @Override
    public ItemStack next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return inv.getStackInSlot(next++);
    }
}
