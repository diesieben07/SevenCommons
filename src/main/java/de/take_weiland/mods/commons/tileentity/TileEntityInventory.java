package de.take_weiland.mods.commons.tileentity;

import de.take_weiland.mods.commons.Listenable;
import de.take_weiland.mods.commons.Listenables;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.util.Blocks;
import de.take_weiland.mods.commons.util.Inventories;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.Block;

/**
 * <p>Basic implementation of a {@link TileEntity} with an Inventory. Similar to
 * {@link de.take_weiland.mods.commons.inv.AbstractInventory}.</p>
 * <p>Note that this class implements {@link de.take_weiland.mods.commons.inv.NameableInventory}, so if you
 * use {@link Blocks#init(Block, String)} on your block (or
 * use {@link de.take_weiland.mods.commons.templates.SCItemBlock} as your custom ItemBlock class), this TileEntity
 * will automatically take the name of a renamed ItemStack when placed. To control that behavior, implement
 * {@link TileAutoName}</p>
 */
public abstract class TileEntityInventory<T extends TileEntityInventory<T>> extends AbstractTileEntity implements IInventory, Listenable<T>, NameableInventory {

	private static final String CUSTOM_NAME_KEY = "_sc$customName";
	private boolean hasName = false;
	private String name;

	/**
	 * Backing ItemStack storage
	 */
	protected final ItemStack[] storage;

	/**
	 * <p>The default constructor calls {@link #getSizeInventory()} to determine the inventory size.</p>
	 * <p>If that is not desired, use {@link TileEntityInventory.WithSize} instead.</p>
	 */
	public TileEntityInventory() {
		storage = new ItemStack[getSizeInventory()];
	}

	TileEntityInventory(int size) {
		storage = new ItemStack[size];
	}

	/**
	 * <p>Get the unlocalized name of this inventory.</p>
	 * @return the unlocalized name
	 */
	protected abstract String unlocalizedName();

	@Override
	public ItemStack getStackInSlot(int slot) {
		return JavaUtils.get(storage, slot);
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
		onInventoryChanged();
	}

	@Override
	public final void onInventoryChanged() {
		super.onInventoryChanged();
		Listenables.onChange(this);
	}

	@Override
	public String getInvName() {
		return hasCustomName() ? getCustomName() : unlocalizedName();
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
		Inventories.readInventory(storage, nbt);
		if (nbt.hasKey(CUSTOM_NAME_KEY)) {
			setCustomName(nbt.getString(CUSTOM_NAME_KEY));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		Inventories.writeInventory(storage, nbt);
		if (hasCustomName()) {
			nbt.setString(CUSTOM_NAME_KEY, getCustomName());
		}
	}
	
	@Override
	public void openChest() { }

	@Override
	public void closeChest() { }

	@Override
	public void onChange() { }

	// NameableInventory
	@Override
	public boolean hasCustomName() {
		return hasName;
	}

	@Override
	public void setCustomName(String name) {
		hasName = true;
		this.name = name;
	}

	@Override
	public String getCustomName() {
		return name;
	}

	/**
	 * Like {@link de.take_weiland.mods.commons.inv.AbstractInventory.WithSize} but for TileEntities
	 */
	public static abstract class WithSize<T extends WithSize<T>> extends TileEntityInventory<T> {

		private final int size;

		public WithSize(int size) {
			super(size);
			this.size = size;
		}

		@Override
		public final int getSizeInventory() {
			return size;
		}
	}

}
