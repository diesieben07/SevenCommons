package de.take_weiland.mods.commons.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public interface AdvancedContainer<T extends IInventory> {

	boolean handlesButton(EntityPlayer player, int buttonId);
	
	void clickButton(Side side, EntityPlayer player, int buttonId);
	
	T inventory();
	
	EntityPlayer getPlayer();
	
	int getMergeTargetSlot(ItemStack stack);
	
	int getFirstPlayerSlot();

	void readSyncData(ByteArrayDataInput in);

	void writeSyncData(ByteArrayDataOutput out, boolean all);

	boolean prepareSyncData();
	
}
