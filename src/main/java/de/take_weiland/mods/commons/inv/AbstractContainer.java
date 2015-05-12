package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>Abstract base class for an inventory Container.</p>
 * <p>This implementation handles one inventory plus the player inventory.</p>
 */
public abstract class AbstractContainer<T extends IInventory> extends Container {

	/**
	 * <p>The inventory being displayed.</p>
	 */
	protected final T inventory;

	/**
	 * <p>The player interacting with this Container.</p>
	 */
	protected final EntityPlayer player;

	/**
	 * <p>Create a new AbstractContainer with the given inventory, player and position for the player inventory.</p>
	 * <p>This constructor first calls {@link #addSlots()} to add any non-player slots and then adds the player inventory
	 * at the given coordinates.</p>
	 * <p>If you pass a negative value for {@code playerInventoryX} the player inventory will not be added.</p>
	 * @param inventory the inventory
	 * @param player the player
	 * @param playerInventoryX the x-coordinate for the player inventory
	 * @param playerInventoryY the y-coordinate for the player inventory
	 */
	public AbstractContainer(T inventory, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
		this.inventory = inventory;
		this.player = player;
		addSlots();
		if (playerInventoryX >= 0) {
			Containers.addPlayerInventory(this, player.inventory, playerInventoryX, playerInventoryY);
		}
		inventory.openInventory();
	}

	/**
	 * <p>Create a new AbstractContainer with the given inventory and player.</p>
	 * <p>This constructor first calls {@link #addSlots()} to add any non-player slots and then adds the player inventory
	 * at the default coordinates.</p>
	 * @param inventory the inventory
	 * @param player the player
	 */
	public AbstractContainer(T inventory, EntityPlayer player) {
		this(inventory, player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT);
	}

	/**
	 * <p>Create a new AbstractContainer with the inventory implemented by the TileEntity at the given position and the
	 * given player.</p>
	 * <p>This constructor first fetches the TileEntity at the given position. This TileEntity must be of type {@code T}.
	 * Then {@link #addSlots()} is called to add any non-player slots and then the player inventory is added at the
	 * default coordinates.
	 * @param world the world
	 * @param x the x coordinate of the TileEntity
	 * @param y the y coordinate of the TileEntity
	 * @param z the z coordinate of the TileEntity
	 * @param player the player
	 */
	public AbstractContainer(World world, int x, int y, int z, EntityPlayer player) {
		this(world, x, y, z, player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT);
	}

	/**
	 * <p>Create a new AbstractContainer with the inventory implemented by the TileEntity at the given position and the
	 * given player.</p>
	 * <p>This constructor first fetches the TileEntity at the given position. This TileEntity must be of type {@code T}.
	 * Then {@link #addSlots()} is called to add any non-player slots and then the player inventory is added at the
	 * given coordinates.</p>
	 * <p>If you pass a negative value for {@code playerInventoryX} the player inventory will not be added.</p>
	 * @param world the world
	 * @param x the x coordinate of the TileEntity
	 * @param y the y coordinate of the TileEntity
	 * @param z the z coordinate of the TileEntity
	 * @param player the player
	 * @param playerInventoryX the x-coordinate for the player inventory
	 * @param playerInventoryY  the y-coordinate for the player inventory
	 */
	@SuppressWarnings("unchecked")
	public AbstractContainer(World world, int x, int y, int z, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
		this((T) world.getTileEntity(x, y, z), player, playerInventoryX, playerInventoryY);
	}

	/**
	 * <p>Add the slots for the inventory to this Container in this method. Note that the player inventory is usually added automatically.</p>
	 */
	protected abstract void addSlots();

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return Containers.handleShiftClick(this, player, slot);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		inventory.closeInventory();
	}
}
