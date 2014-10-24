package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class SCPlayerTicker implements ITickHandler {

	public static final String INV_IN_USE_KEY = "_sc$iteminv$inUse";

	@Override
	public void tickStart(EnumSet<TickType> type, Object... data) {
		EntityPlayer player = (EntityPlayer) data[0];
		if (player.openContainer != player.inventoryContainer) {
			return;
		}
		ItemStack current = player.inventory.getCurrentItem();
		if (current != null && current.stackTagCompound != null) {
			current.stackTagCompound.removeTag(INV_IN_USE_KEY);
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) { }

	private static final EnumSet<TickType> ticks = EnumSet.of(TickType.PLAYER);
	@Override
	public EnumSet<TickType> ticks() {
		return ticks;
	}

	@Override
	public String getLabel() {
		return null;
	}
}
