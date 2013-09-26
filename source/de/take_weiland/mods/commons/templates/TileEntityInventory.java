package de.take_weiland.mods.commons.templates;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.Lists;

import de.take_weiland.mods.commons.util.Inventories;

public abstract class TileEntityInventory extends TileEntityAbstract implements AdvancedInventory {

	private List<AdvancedInventory.Listener> listeners;
	protected final ItemStack[] storage;
	
	public TileEntityInventory() {
		storage = provideStorage();
	}
	
	@Override
	public ItemStack[] getItemStorage() {
		return storage;
	}

	protected ItemStack[] provideStorage() {
		return new ItemStack[getSizeInventory()];
	}
	
	protected abstract String getDefaultName();
	
	@Override
	public ItemStack getStackInSlot(int slot) {
		return storage[slot];
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
		storage[slot] = item;
		onChange();
	}

	@Override
	public String getInvName() {
		return hasCustomName() ? getCustomName() : getDefaultName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return !hasCustomName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord, yCoord, zCoord) <= 64;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		return true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		Inventories.readInventory(storage, nbt.getTagList("items"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setTag("items", Inventories.writeInventory(storage));
	}
	
	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

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

	@Override
	public void onChange() {
		if (listeners != null) {
			for (Listener listener : listeners) {
				listener.onInventoryChanged(this);
			}
		}
	}

}
