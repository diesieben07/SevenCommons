package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(PacketViewUpdates packet);

	public void handleModState(PacketModState packet);

	public void handleDownloadProgress(PacketDownloadProgress packet);
	
}
