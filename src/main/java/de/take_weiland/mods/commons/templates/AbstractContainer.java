package de.take_weiland.mods.commons.templates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.PacketInventoryName;
import de.take_weiland.mods.commons.util.Containers;
import de.take_weiland.mods.commons.util.Sides;

public abstract class AbstractContainer<T extends IInventory> extends Container implements SCContainer<T> {

	protected final T inventory;
	
	protected final EntityPlayer player;
	
	private final int firstPlayerSlot;
	
	protected AbstractContainer(T upper, EntityPlayer player) {
		this(upper, player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT);
		upper.openChest();
	}
	
	protected AbstractContainer(T upper, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
		inventory = upper;
		this.player = player;
		addSlots();
		if (playerInventoryX >= 0) {
			firstPlayerSlot = inventorySlots.size();
			Containers.addPlayerInventory(this, player.inventory, playerInventoryX, playerInventoryY);
		} else {
			firstPlayerSlot = -1;
		}
	}
	
	protected AbstractContainer(World world, int x, int y, int z, EntityPlayer player) {
		this(world, x, y, z, player, Containers.PLAYER_INV_X_DEFAULT, Containers.PLAYER_INV_Y_DEFAULT);
	}
	
	@SuppressWarnings("unchecked")
	protected AbstractContainer(World world, int x, int y, int z, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
		this((T) world.getBlockTileEntity(x, y, z), player, playerInventoryX, playerInventoryY);
	}
	
	@Override
	public int getFirstPlayerSlot() {
		return firstPlayerSlot;
	}

	protected abstract void addSlots();
	
	protected int getSlotFor(ItemStack item) {
		return -1;
	}
	
	@Override
	public int[] getSlotRange(ItemStack item) {
		int target = getSlotFor(item);
		return target == -1 ? null : new int[] { target, target + 1 };
	}
	
	@Override
	public final T inventory() {
		return inventory;
	}
	
	@Override
	public EntityPlayer getPlayer() {
		return player;
	}
	
	@Override
	public boolean handlesButton(EntityPlayer player, int buttonId) {
		return false;
	}
	
	@Override
	public void onButtonClick(Side side, EntityPlayer player, int buttonId) { }

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		id = UnsignedBytes.toInt((byte)id);
		onButtonClick(Sides.logical(player), player, id);
		return true;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		return Containers.transferStack(this, player, slot);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		inventory.closeChest();
	}
	
	public static abstract class Synced<T extends IInventory> extends AbstractContainer<T> implements SyncedContainer<T> {

		protected Synced(T upper, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
			super(upper, player, playerInventoryX, playerInventoryY);
		}

		protected Synced(T upper, EntityPlayer player) {
			super(upper, player);
		}

		protected Synced(World world, int x, int y, int z, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
			super(world, x, y, z, player, playerInventoryX, playerInventoryY);
		}

		protected Synced(World world, int x, int y, int z, EntityPlayer player) {
			super(world, x, y, z, player);
		}

		@Override
		public void detectAndSendChanges() {
			super.detectAndSendChanges();
			Containers.sync(this);
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting player) {
		super.addCraftingToCrafters(player);
		if (inventory instanceof NameableTileEntity && ((NameableTileEntity) inventory).hasCustomName() && player instanceof EntityPlayerMP) {
			new PacketInventoryName(windowId, ((NameableTileEntity) inventory).getCustomName()).sendTo((EntityPlayer) player);
		}
	}
	
}
