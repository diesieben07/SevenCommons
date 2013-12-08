package de.take_weiland.mods.commons.templates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;

public interface SCContainer<T extends IInventory> {

	boolean handlesButton(EntityPlayer player, int buttonId);
	
	T inventory();
	
	EntityPlayer getPlayer();
	
	int[] getSlotRange(ItemStack stack);
	
	int getFirstPlayerSlot();

	void onButtonClick(Side side, EntityPlayer player, int buttonId);

}
