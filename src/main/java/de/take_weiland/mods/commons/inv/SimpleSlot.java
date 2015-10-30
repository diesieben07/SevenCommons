package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.internal.ContainerAwareSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * <p>A basic implementation of {@link Slot} that delegates {@link Slot#isItemValid(ItemStack)} to
 * {@link IInventory#isItemValidForSlot(int, ItemStack)}.</p>
 * <p>This class also checks for {@link ItemInventory#canTakeStack(Container, ItemStack, EntityPlayer)}.</p>
 */
public class SimpleSlot extends Slot implements ContainerAwareSlot {

    private Container container;
    private final int xNormal, yNormal;

    public SimpleSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        xNormal = x;
        yNormal = y;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(slotNumber, stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return container != null && ItemInventory.canTakeStack(container, getStack(), player);
    }

    @Override
    public void _sc$injectContainer(Container container) {
        this.container = container;
    }

    public void setNormalPosition() {
        xDisplayPosition = xNormal;
        yDisplayPosition = yNormal;
    }

    public void setDisplayPosition(int x, int y) {
        xDisplayPosition = x;
        yDisplayPosition = y;
    }
}
