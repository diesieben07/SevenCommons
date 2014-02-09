package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import cpw.mods.fml.common.IPlayerTracker;
import de.take_weiland.mods.commons.sync.SyncASMHooks;

public final class SCEventHandler implements IPlayerTracker {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onEntityConstruct(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityPlayerMP) {
			SyncASMHooks.syncEntityPropertyIds((EntityPlayer) event.entity, event.entity);
		}
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		//SyncASMHooks.syncEntityPropertyIds(player, player);
	}
	
	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		SyncASMHooks.syncEntityPropertyIds(player, player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) { }

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) { }

}
