package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * @author diesieben07
 */
public class SimpleContainer<T extends IInventory> extends AbstractContainer<T> {

    public SimpleContainer(T upper, EntityPlayer player) {
        // suppress player inventory addition
        super(upper, player, -1, -1);
    }

    @Override
    protected final void addSlots() {
    }

    public final SimpleContainer<T> addSlot(Slot slot) {
        addSlotToContainer(slot);
        return this;
    }

    public final SimpleContainer<T> addPlayerInventory() {
        Containers.addPlayerInventory(this, player.inventory);
        return this;
    }

    public final SimpleContainer<T> addPlayerInventory(int x, int y) {
        Containers.addPlayerInventory(this, player.inventory, x, y);
        return this;
    }

}
