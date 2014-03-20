package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(UpdateController controller);

	public void shutdownMinecraft();
	
	public void displayRestartFailure();
	
	INetworkManager getNetworkManagerFromClient(NetHandler clientHandler);

	void refreshUpdatesGui();
}
