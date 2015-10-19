package de.take_weiland.mods.commons.tileentity;

import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.inv.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * <p>Basic implementation of a {@link net.minecraft.tileentity.TileEntity} with an Inventory.
 * <p>Note that this class implements {@link de.take_weiland.mods.commons.inv.NameableInventory}, so this TileEntity
 * will automatically take the name of a renamed ItemStack when placed. To control that behavior, override
 * {@link #takeItemStackName(EntityPlayer, ItemStack)}.</p>
 */
public abstract class TileEntityInventory extends TileEntity implements SimpleInventory, NameableInventory {

    private String name;

    /**
     * Backing ItemStack storage
     */
    protected final ItemStack[] storage;

    /**
     * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
     */
    protected TileEntityInventory() {
        storage = new ItemStack[getSizeInventory()];
    }

    /**
     * <p>Alternate constructor that doesn't need {@link #getSizeInventory()} to be overridden.</p>
     *
     * @param size the size of this inventory
     */
    protected TileEntityInventory(int size) {
        storage = new ItemStack[size];
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage[slot];
    }

    @Override
    public void setSlotNoMark(int slot, ItemStack stack) {
        storage[slot] = stack;
    }

    @Override
    public int getSizeInventory() {
        return storage.length;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord, yCoord, zCoord) <= 64;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        SimpleInventory.super.readFromNBT(nbt);
        NameableInventory.super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        SimpleInventory.super.writeToNBT(nbt);
        NameableInventory.super.writeToNBT(nbt);
    }

    // NameableInventory
    @Override
    public boolean hasCustomName() {
        return name != null;
    }

    @Override
    public void setCustomName(@Nonnull String name) {
        this.name = name;
    }

    @Override
    public String getCustomName() {
        return name;
    }

    @Nonnull
    @Override
    public Iterator<ItemStack> iterator() {
        return Iterators.forArray(storage);
    }

    @Override
    public Spliterator<ItemStack> spliterator() {
        return Arrays.spliterator(storage);
    }

    @Override
    public void forEach(@Nonnull Consumer<? super ItemStack> action) {
        for (ItemStack stack : storage) {
            action.accept(stack);
        }
    }
}
