package de.take_weiland.mods.commons.inv;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public interface SCContainer<T extends IInventory> {

	boolean isContainerButton(EntityPlayer player, int buttonId);
	
	T inventory();
	
	EntityPlayer getPlayer();
	
	long getSlotRange(ItemStack stack);
	
	int getFirstPlayerSlot();

	void onButtonClick(Side side, EntityPlayer player, int buttonId);

}
