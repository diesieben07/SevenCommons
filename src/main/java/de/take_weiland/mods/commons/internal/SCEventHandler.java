package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class SCEventHandler implements IPlayerTracker {

	@ForgeSubscribe(priority = EventPriority.LOWEST)
	public void onEntityConstruct(EntityEvent.EntityConstructing event) {
		if (event.entity instanceof EntityPlayerMP) {
			SyncASMHooks.syncEntityPropertyIds((EntityPlayer) event.entity, event.entity);
		}
	}

	@ForgeSubscribe
	public void onEntityDeath(LivingDeathEvent event) {
		ServerProxy.resetUpdateViewer(event.entity);
	}

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		SyncASMHooks.syncEntityPropertyIds(player, player);
	}
	
	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		SyncASMHooks.syncEntityPropertyIds(player, player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		ServerProxy.resetUpdateViewer(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		ServerProxy.resetUpdateViewer(player);
	}

}
