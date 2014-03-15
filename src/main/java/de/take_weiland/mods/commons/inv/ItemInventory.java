package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.item.ItemStacks;
import de.take_weiland.mods.commons.util.NBT;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>an inventory that stores its contents to an {@link net.minecraft.item.ItemStack}</p>
 * <p>If the ItemStack being written to is in an inventory itself, {@link ItemInventory.WithInventory} should be used instead.</p>
 */
public abstract class ItemInventory<T extends ItemInventory<T>> extends AbstractInventory<T> {

	/**
	 * the default NBT key used for storing the data
	 */
	public static final String DEFAULT_NBT_KEY = "_sc$itemInventory";

	/**
	 * the ItemStack this inventory stores it's data to
	 */
	protected final ItemStack stack;
	/**
	 * the NBT key to store data in
	 */
	protected final String nbtKey;

	/**
	 * this constructor uses the given NBT key to store the data
	 * @param item the ItemStack to save to
	 * @param nbtKey the NBT key to use
	 */
	protected ItemInventory(ItemStack item, String nbtKey) {
		stack = item;
		this.nbtKey = nbtKey;
		readFromNbt(getNbt());
	}

	/**
	 * this constructor uses the {@link #DEFAULT_NBT_KEY} to store the data
	 * @param item the ItemStack to save to
	 */
	protected ItemInventory(ItemStack item) {
		this(item, DEFAULT_NBT_KEY);
	}

	@Override
	public void onChange() {
		super.onChange();
		saveData();
	}

	/**
	 * saves this inventory to the ItemStack.
	 */
	protected final void saveData() {
		writeToNbt(getNbt());
	}

	private NBTTagCompound getNbt() {
		return NBT.getOrCreateCompound(ItemStacks.getNbt(stack), nbtKey);
	}

	public abstract static class WithInventory<T extends WithInventory<T>> extends ItemInventory<T> {

		public final int slot;
		public final IInventory inv;

		protected WithInventory(IInventory inv, int slot, String nbtKey) {
			// hell yeah
			super(checkNotNull(checkNotNull(inv, "Inventory must not be null!").getStackInSlot(slot), "Inventory slot is empty!"), nbtKey);
			this.slot = slot;
			this.inv = inv;
		}
		
		protected WithInventory(IInventory inv, int index) {
			this(inv, index, DEFAULT_NBT_KEY);
		}

		@Override
		public void onChange() {
			super.onChange();
			inv.setInventorySlotContents(slot, stack);
		}

	}

}
