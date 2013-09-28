package de.take_weiland.mods.commons.templates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;

public interface AdvancedContainer<T extends IInventory> {

	boolean handlesButton(EntityPlayer player, int buttonId);
	
	T inventory();
	
	EntityPlayer getPlayer();
	
	int getMergeTargetSlot(ItemStack stack);
	
	int getFirstPlayerSlot();

	void clickButton(Side side, EntityPlayer player, int buttonId);

	public abstract void readSyncData(DataInputStream in) throws IOException;

	public abstract void writeSyncData(DataOutputStream out) throws IOException;

	public abstract boolean isSynced();
	
}
