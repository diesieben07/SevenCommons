package de.take_weiland.mods.commons.inv;

import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>An inventory that stores its contents to an {@link net.minecraft.item.ItemStack}</p>
 * <p>If the ItemStack being written to is in an inventory itself, {@link ItemInventory.WithInventory} should be used instead.</p>
 */
public abstract class ItemInventory<T extends ItemInventory<T>> extends AbstractInventory {

	/**
	 * <p>The default NBT key used for storing the data.</p>
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
	 * <p>This constructor uses the given NBT key to store the data.</p>
     * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
	 * @param stack the ItemStack to save to
	 * @param nbtKey the NBT key to use
	 */
	protected ItemInventory(ItemStack stack, String nbtKey) {
        super();
		this.stack = stack;
		this.nbtKey = nbtKey;
		readFromNbt(getNbt());
	}

    /**
     * <p>This constructor uses the given NBT key to store the data.</p>
     * @param size the size of this inventory
     * @param stack the ItemStack to save to
     * @param nbtKey the NBT key to use
     */
    protected ItemInventory(int size, ItemStack stack, String nbtKey) {
        super(size);
        this.stack = stack;
        this.nbtKey = nbtKey;
        readFromNbt(getNbt());
    }

	/**
	 * <p>This constructor uses the {@link #DEFAULT_NBT_KEY} to store the data.</p>
     * <p>This constructor calls {@link #getSizeInventory()} to determine the size of the inventory. It needs to be overridden and work properly when called from this constructor.</p>
	 * @param item the ItemStack to save to
	 */
	protected ItemInventory(ItemStack item) {
		this(item, DEFAULT_NBT_KEY);
	}

    /**
     * <p>This constructor uses the {@link #DEFAULT_NBT_KEY} to store the data.</p>
     * @param size the size of this inventory
     * @param item the ItemStack to save to
     */
    protected ItemInventory(int size, ItemStack item) {
        this(size, item, DEFAULT_NBT_KEY);
    }

    @Override
    public void onInventoryChanged() {
        saveData();
    }

    /**
	 * saves this inventory to the ItemStack.
	 */
	protected final void saveData() {
		writeToNbt(getNbt());
	}

	private NBTTagCompound getNbt() {
		return ItemStacks.getNbt(stack, nbtKey);
	}

	public abstract static class WithInventory<T extends WithInventory<T>> extends ItemInventory<T> {

		public final int slot;
		public final IInventory inv;

		protected WithInventory(IInventory inv, int slot, String nbtKey) {
			// hell yeah
			super(checkStack(inv, slot), nbtKey);
			this.slot = slot;
			this.inv = inv;
		}

        protected WithInventory(int size, IInventory inv, int slot, String nbtKey) {
            super(size, checkStack(inv, slot), nbtKey);
            this.slot = slot;
            this.inv = inv;
        }

        protected WithInventory(IInventory inv, int index) {
            this(inv, index, DEFAULT_NBT_KEY);
        }

        protected WithInventory(int size, IInventory inv, int index) {
            this(size, inv, index, DEFAULT_NBT_KEY);
        }

        private static ItemStack checkStack(IInventory inv, int slot) {
            return checkNotNull(checkNotNull(inv, "Inventory must not be null!").getStackInSlot(slot), "Inventory slot is empty!");
        }

        @Override
        public void onInventoryChanged() {
            super.onInventoryChanged();
            inv.setInventorySlotContents(slot, stack);
        }

	}

}
