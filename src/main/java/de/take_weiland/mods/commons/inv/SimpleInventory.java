package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * <p>A skeletal implementation for {@link IInventory} which implements those methods which usually do not need to be overridden.</p>
 * <p>In addition this interface extends {@link Iterable} and allows iterating all ItemStacks in the inventory.</p>
 *
 * @author diesieben07
 */
public interface SimpleInventory extends IInventory, Iterable<ItemStack> {

    @Override
    default ItemStack decrStackSize(int slot, int count) {
        return Inventories.decreaseStackSize(this, slot, count);
    }

    @Override
    default ItemStack getStackInSlotOnClosing(int slot) {
        return Inventories.getAndRemove(this, slot);
    }

    @Override
    default int getInventoryStackLimit() {
        return 64;
    }

    @Override
    default boolean isItemValidForSlot(int slot, @Nullable ItemStack stack) {
        return true;
    }

    /**
     * <p>Like {@link #setInventorySlotContents(int, ItemStack)}, but does not call {@link #markDirty()}.</p>
     * <p>This method should be implemented instead of the usual {@link #setInventorySlotContents(int, ItemStack)},
     * which is implemented in terms of this method and {@link #markDirty()}.</p>
     *
     * @param slot  the slot
     * @param stack the ItemStack
     */
    void setSlotNoMark(int slot, @Nullable ItemStack stack);

    @Override
    default void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
        setSlotNoMark(slot, stack);
        markDirty();
    }

    @Override
    default void markDirty() {
    }

    @Override
    default void openChest() {
    }

    @Override
    default void closeChest() {
    }

    @Override
    default Iterator<ItemStack> iterator() {
        return new InventoryIterator(this);
    }

    @Override
    default void forEach(Consumer<? super ItemStack> action) {
        Inventories.doForEach(this, action);
    }

    @Override
    default Spliterator<ItemStack> spliterator() {
        return new InventorySpliterator(this, 0, getSizeInventory());
    }

    default void readFromNBT(NBTTagCompound nbt) {
        int size = getSizeInventory();

        NBTTagList list = nbt.getTagList(Inventories.NBT_KEY, NBT.TAG_COMPOUND);
        for (int i = 0, len = list.tagCount(); i < len; i++) {
            NBTTagCompound slotNBT = list.getCompoundTagAt(i);
            int slot = slotNBT.getInteger("slot");
            if (slot >= 0 && slot < size) {
                setSlotNoMark(slot, ItemStack.loadItemStackFromNBT(slotNBT));
            }
        }
    }

    default void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (int slot = 0, size = getSizeInventory(); slot < size; slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack != null) {
                NBTTagCompound slotNBT = new NBTTagCompound();
                slotNBT.setInteger("slot", slot);
                stack.writeToNBT(slotNBT);
                list.appendTag(slotNBT);
            }
        }
        nbt.setTag(Inventories.NBT_KEY, list);
    }
}
