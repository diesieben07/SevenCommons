package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractInventory<T extends AbstractInventory<T>> implements SCInventory<T> {

	@SuppressWarnings("unchecked")
	private final ListenerList<T> listeners = ListenerArrayList.create((T)this);
	
	protected final ItemStack[] storage;
	
	protected AbstractInventory(int size) {
		storage = new ItemStack[size];
	}
	
	protected AbstractInventory() {
		storage = new ItemStack[getSizeInventory()];
	}
	
	@Override
	public ItemStack[] getItemStorage() {
		return storage;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return JavaUtils.safeArrayAccess(storage, slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		return Inventories.decreaseStackSize(this, slot, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return Inventories.getAndRemove(this, slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		if (slot >= 0 && slot < storage.length) {
			storage[slot] = item;
		}
		onChange();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	@Override
	public void onInventoryChanged() { }
	
	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		return true;
	}

	public void writeToNbt(NBTTagCompound nbt) {
		nbt.setTag("slots", Inventories.writeInventory(storage));
	}

	public void readFromNbt(NBTTagCompound nbt) {
		Inventories.readInventory(storage, nbt.getTagList("slots"));
	}
	
	@Override
	public void onChange() {
		listeners.onChange();
	}

	@Override
	public void addListener(Listenable.Listener<? super T> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listenable.Listener<? super T> listener) {
		listeners.remove(listener);
	}
}
