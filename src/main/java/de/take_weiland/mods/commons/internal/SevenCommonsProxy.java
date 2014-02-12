package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(PacketViewUpdates packet);

	public void handleModState(PacketModState packet);

	public void handleDownloadProgress(PacketDownloadProgress packet);

	public void shutdownMinecraft();
	
	public void displayRestartFailure();
	
	INetworkManager getNetworkManagerFromClient(NetHandler clientHandler);
	
}
