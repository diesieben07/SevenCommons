package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.Listenable;
import de.take_weiland.mods.commons.Listenables;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * <p>Basic implementation of {@link net.minecraft.inventory.IInventory}.</p>
 * <p>This implementation also implements {@link de.take_weiland.mods.commons.Listenable}. By default, listeners
 * will be notified when {@link #onInventoryChanged()} is called.</p>
 */
public abstract class AbstractInventory<T extends AbstractInventory<T>> implements IInventory, Listenable<T> {

	/**
	 * Backing storage array
	 */
	protected final ItemStack[] storage;

	/**
	 * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
	 */
	protected AbstractInventory() {
		storage = new ItemStack[getSizeInventory()];
	}

    /**
     * <p>Alternate constructor that doesn't need {@link #getSizeInventory()} to be overridden.</p>
     * @param size the size of this inventory
     */
	protected AbstractInventory(int size) {
		storage = new ItemStack[size];
	}

    @Override
    public int getSizeInventory() {
        return storage.length;
    }

    @Override
	public ItemStack getStackInSlot(int slot) {
		return JavaUtils.get(storage, slot);
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation uses {@link Inventories#decreaseStackSize(net.minecraft.inventory.IInventory, int, int)}</p>
	 */
	@Override
	public ItemStack decrStackSize(int slot, int count) {
		return Inventories.decreaseStackSize(this, slot, count);
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation uses {@link Inventories#getAndRemove(net.minecraft.inventory.IInventory, int)}</p>
	 */
	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return Inventories.getAndRemove(this, slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		if (slot >= 0 && slot < storage.length) {
			storage[slot] = item;
		}
		onInventoryChanged();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	@Override
	public final void onInventoryChanged() {
		Listenables.onChange(this);
	}
	
	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		return true;
	}

	/**
	 * <p>Writes this inventory to an {@link net.minecraft.nbt.NBTTagCompound}.</p>
	 * <p>By default calls {@link Inventories#writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound)}</p>
	 * @param nbt the NBTTagCompound to write to
	 */
	public void writeToNbt(NBTTagCompound nbt) {
		Inventories.writeInventory(storage, nbt);
	}

	/**
	 * <p>Reads this inventory from an {@link net.minecraft.nbt.NBTTagCompound}.</p>
	 * <p>By default calls {@link Inventories#readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound)}</p>
	 * @param nbt the NBTTagCompound to read from
	 */
	public void readFromNbt(NBTTagCompound nbt) {
		Inventories.readInventory(storage, nbt);
	}

	@Override
	public void onChange() { }

}
