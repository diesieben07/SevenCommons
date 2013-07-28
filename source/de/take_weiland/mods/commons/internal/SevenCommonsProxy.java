package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.internal_t.network.PacketDownloadProgress;
import de.take_weiland.mods.commons.internal_t.network.PacketModState;
import de.take_weiland.mods.commons.internal_t.network.PacketViewUpdates;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(PacketViewUpdates packet);

	public void handleModState(PacketModState packet);

	public void handleDownloadProgress(PacketDownloadProgress packet);
	
}
