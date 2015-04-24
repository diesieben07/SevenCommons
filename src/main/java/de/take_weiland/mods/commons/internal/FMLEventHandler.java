package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public final class FMLEventHandler {

	public static final String INV_IN_USE_KEY = "_sc$iteminv$inUse";

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START && event.side.isServer() && event.player.openContainer == event.player.inventoryContainer) {
			ItemStack current = event.player.inventory.getCurrentItem();
			if (current != null && current.stackTagCompound != null) {
				current.stackTagCompound.removeTag(INV_IN_USE_KEY);
			}
		}
	}

}
