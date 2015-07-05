package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

/**
 * <p>A simple implementation of a {@link net.minecraft.inventory.Slot Slot} that does not allow any Item to be picked up.</p>
 */
public class SlotNoPickup extends SimpleSlot {

    public SlotNoPickup(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }
}
