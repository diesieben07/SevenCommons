package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.Listenables;
import de.take_weiland.mods.commons.util.Inventories;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileEntityInventory<T extends TileEntityInventory<T>> extends AbstractTileEntity implements SCInventory<T> {

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
		Listenables.onChange(this);
	}

	@Override
	public String getInvName() {
		return hasCustomName() ? getCustomName() : getDefaultName();
	}

	@Override
	public boolean isInvNameLocalized() {
		return hasCustomName();
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
	public void onChange() { }

}
