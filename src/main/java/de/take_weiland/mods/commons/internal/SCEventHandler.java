package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.player.EntityPlayer;

public final class SCEventHandler implements IPlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		SyncASMHooks.syncEntityPropertyIds(player, player);
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
