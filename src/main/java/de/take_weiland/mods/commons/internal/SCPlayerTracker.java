package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import de.take_weiland.mods.commons.internal.sync.PacketSyncProperties;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class SCPlayerTracker implements IPlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		PacketSyncProperties.sendSyncedProperties(player, player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		PacketSyncProperties.sendSyncedProperties(player, player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		PacketSyncProperties.sendSyncedProperties(player, player);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) { }
}
