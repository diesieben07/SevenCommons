package de.take_weiland.mods.commons.templates;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.Inventories;

public abstract class AbstractInventory implements AdvancedInventory {

	private List<AdvancedInventory.Listener> listeners;
	
	protected final ItemStack[] storage;
	
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
	public void onChange() {
		if (listeners != null) {
			for (Listener listener : listeners) {
				listener.onInventoryChanged(this);
			}
		}
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

	@Override
	public void registerListener(Listener listener) {
		if (listeners == null) {
			listeners = Lists.newArrayListWithCapacity(3);
		}
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	protected void writeToNbt(NBTTagCompound nbt) {
		nbt.setTag("slots", Inventories.writeInventory(storage));
	}

	protected void readFromNbt(NBTTagCompound nbt) {
		Inventories.readInventory(storage, nbt.getTagList("slots"));
	}

}
