package de.take_weiland.mods.commons.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public interface ListenableInventory extends IInventory {

    void addListener(Listener listener);

    void removeListener(Listener listener);

    interface Listener {

        void onChange(ListenableInventory inv);

        void slotChange(ListenableInventory inv, int slot, @Nullable ItemStack oldStack, @Nullable ItemStack newStack);

    }

}
