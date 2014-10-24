package de.take_weiland.mods.commons.inv;

import com.google.common.collect.Lists;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.List;

/**
 * <p>Basic implementation of {@link net.minecraft.inventory.IInventory}.</p>
 */
public abstract class AbstractInventory implements IInventory, ListenableInventory {

	/**
	 * Backing storage array
	 */
	protected final ItemStack[] storage;
	private List<Listener> listeners;

	/**
	 * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
	 */
	protected AbstractInventory() {
		storage = new ItemStack[getSizeInventory()];
	}

	/**
	 * <p>Alternate constructor that doesn't need {@link #getSizeInventory()} to be overridden.</p>
	 *
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
		return storage[slot];
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
	public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
		if (listeners != null) {
			ItemStack old = storage[slot];
			storage[slot] = stack;

			for (Listener listener : listeners) {
				listener.slotChange(this, slot, old, stack);
			}
		} else {
			storage[slot] = stack;
		}
		onInventoryChanged();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		return true;
	}

	@Override
	public void onInventoryChanged() {
		if (listeners != null) {
			for (Listener listener : listeners) {
				listener.onChange(this);
			}
		}
	}

	/**
	 * <p>Writes this inventory to an {@link net.minecraft.nbt.NBTTagCompound}.</p>
	 * <p>By default calls {@link Inventories#writeInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound)}</p>
	 *
	 * @param nbt the NBTTagCompound to write to
	 */
	public void writeToNbt(NBTTagCompound nbt) {
		Inventories.writeInventory(storage, nbt);
	}

	/**
	 * <p>Reads this inventory from an {@link net.minecraft.nbt.NBTTagCompound}.</p>
	 * <p>By default calls {@link Inventories#readInventory(net.minecraft.item.ItemStack[], net.minecraft.nbt.NBTTagCompound)}</p>
	 *
	 * @param nbt the NBTTagCompound to read from
	 */
	public void readFromNbt(NBTTagCompound nbt) {
		Inventories.readInventory(storage, nbt);
	}

	@Override
	public void removeListener(Listener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public void addListener(Listener listener) {
		(listeners == null ? (listeners = Lists.newArrayListWithExpectedSize(1)) : listeners).add(listener);
	}
}
