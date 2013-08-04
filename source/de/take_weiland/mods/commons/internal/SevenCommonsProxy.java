package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.internal.network.PacketDownloadProgress;
import de.take_weiland.mods.commons.internal.network.PacketModState;
import de.take_weiland.mods.commons.internal.network.PacketViewUpdates;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(PacketViewUpdates packet);

	public void handleModState(PacketModState packet);

	public void handleDownloadProgress(PacketDownloadProgress packet);
	
}
