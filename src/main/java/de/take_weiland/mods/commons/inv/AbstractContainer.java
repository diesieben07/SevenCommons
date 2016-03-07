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
     *
     * @param player           the player
     * @param playerInventoryX the x-coordinate for the player inventory
     * @param playerInventoryY the y-coordinate for the player inventory
     * @param inventory        the inventory
     */
    public AbstractContainer(EntityPlayer player, int playerInventoryX, int playerInventoryY, T inventory) {
        this.inventory = inventory;
        this.player = player;
        addSlots();
        if (playerInventoryX >= 0) {
            Containers.addPlayerInventory(this, player.inventory, playerInventoryX, playerInventoryY);
        }
        inventory.openChest();
    }

    /**
     * <p>Create a new AbstractContainer with the given inventory and player.</p>
     * <p>This constructor first calls {@link #addSlots()} to add any non-player slots and then adds the player inventory
     * at the default coordinates.</p>
     *
     * @param player    the player
     * @param inventory the inventory
     */
    public AbstractContainer(EntityPlayer player, T inventory) {
        this(player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT, inventory);
    }

    /**
     * <p>Create a new AbstractContainer with the inventory implemented by the TileEntity at the given position and the
     * given player.</p>
     * <p>This constructor first fetches the TileEntity at the given position. This TileEntity must be of type {@code T}.
     * Then {@link #addSlots()} is called to add any non-player slots and then the player inventory is added at the
     * default coordinates.
     *
     * @param player the player
     * @param world  the world
     * @param x      the x coordinate of the TileEntity
     * @param y      the y coordinate of the TileEntity
     * @param z      the z coordinate of the TileEntity
     */
    public AbstractContainer(EntityPlayer player, World world, int x, int y, int z) {
        this(player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT, world, x, y, z);
    }

    /**
     * <p>Create a new AbstractContainer with the inventory implemented by the TileEntity at the given position and the
     * given player.</p>
     * <p>This constructor first fetches the TileEntity at the given position. This TileEntity must be of type {@code T}.
     * Then {@link #addSlots()} is called to add any non-player slots and then the player inventory is added at the
     * given coordinates.</p>
     * <p>If you pass a negative value for {@code playerInventoryX} the player inventory will not be added.</p>
     *
     * @param player           the player
     * @param playerInventoryX the x-coordinate for the player inventory
     * @param playerInventoryY the y-coordinate for the player inventory
     * @param world            the world
     * @param x                the x coordinate of the TileEntity
     * @param y                the y coordinate of the TileEntity
     * @param z                the z coordinate of the TileEntity
     */
    @SuppressWarnings("unchecked")
    public AbstractContainer(EntityPlayer player, int playerInventoryX, int playerInventoryY, World world, int x, int y, int z) {
        this(player, playerInventoryX, playerInventoryY, (T) world.getTileEntity(x, y, z));
    }

    /**
     * <p>Get this container's inventory.</p>
     *
     * @return the inventory
     */
    public T inventory() {
        return inventory;
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
        inventory.closeChest();
    }
}
