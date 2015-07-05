package de.take_weiland.mods.commons.inv;

import com.google.common.collect.ImmutableSet;
import de.take_weiland.mods.commons.internal.ContainerAwareSlot;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.UUID;

/**
 * <p>A basic implementation of {@link Slot} that delegates {@link Slot#isItemValid(ItemStack)} to
 * {@link IInventory#isItemValidForSlot(int, ItemStack)}.</p>
 */
public class SimpleSlot extends Slot implements ContainerAwareSlot {

    private Container container;

    public SimpleSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return inventory.isItemValidForSlot(slotNumber, stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        if (container == null) {
            return true;
        }

        ItemStack stack = getStack();
        if (stack == null) {
            return true;
        }

        UUID uuid = NBTData.readUUID(ItemStacks.getNbt(stack, ItemInventory.NBT_UUID_KEY));
        if (uuid == null) {
            return true;
        }

        ImmutableSet<IInventory> inventories = Containers.getInventories(container);
        for (IInventory inventory : inventories) {
            if (inventory instanceof ItemInventory && uuid.equals(((ItemInventory) inventory).uuid)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void _sc$injectContainer(Container container) {
        this.container = container;
    }
}
