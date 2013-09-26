package de.take_weiland.mods.commons.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.Containers;
import de.take_weiland.mods.commons.util.Sides;

public abstract class AbstractContainer<T extends IInventory> extends Container implements AdvancedContainer<T> {

	protected final T inventory;
	
	protected final EntityPlayer player;
	
	private final int firstPlayerSlot;
	
	protected AbstractContainer(T upper, EntityPlayer player) {
		this(upper, player, 8, 84);
		upper.openChest();
	}
	
	protected AbstractContainer(T upper, EntityPlayer player, int playerInventoryX, int playerInventoryY) {
		inventory = upper;
		this.player = player;
		addSlots();
		firstPlayerSlot = inventorySlots.size();
		Containers.addPlayerInventory(this, player.inventory, playerInventoryX, playerInventoryY);
	}
	
	protected AbstractContainer(World world, int x, int y, int z, EntityPlayer player) {
		this(world, x, y, z, player, 8, 84);
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
	public void clickButton(Side side, EntityPlayer player, int buttonId) { }

	@Override
	public boolean enchantItem(EntityPlayer player, int id) {
		id = UnsignedBytes.toInt((byte)id);
		clickButton(Sides.logical(player), player, id);
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
	
	@Override
	public boolean isSynced() {
		return false;
	}
	
	@Override
	public void writeSyncData(DataOutputStream out) throws IOException { }
	
	@Override
	public void readSyncData(DataInputStream in) throws IOException { }

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		Containers.sync(this);
	}

}
