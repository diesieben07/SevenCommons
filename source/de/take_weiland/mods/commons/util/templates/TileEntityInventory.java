package de.take_weiland.mods.commons.util.templates;

import de.take_weiland.mods.commons.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public abstract class TileEntityInventory extends TileEntityAbstract<TileEntityInventory> implements IInventory {

	private ItemStack[] storage;
	
	public TileEntityInventory() {
		storage = provideStorage();
	}
	
	protected abstract ItemStack[] provideStorage();
	
	protected abstract String getDefaultName();
	
	@Override
	public ItemStack getStackInSlot(int slot) {
		return storage[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		return InventoryUtils.decreaseStackSize(this, slot, count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return InventoryUtils.getAndRemove(this, slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack item) {
		storage[slot] = item;
		onInventoryChanged();
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
	public void onInventoryChanged() { }

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord, yCoord, zCoord) <= 64;
	}

	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack item) {
		return true;
	}
	
}
