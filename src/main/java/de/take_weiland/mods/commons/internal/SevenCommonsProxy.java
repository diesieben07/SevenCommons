package de.take_weiland.mods.commons.internal;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public interface SevenCommonsProxy {
	
	public void preInit(FMLPreInitializationEvent event);

	public void handleViewUpdates(PacketViewUpdates packet);

	public void handleModState(PacketModState packet);

	public void handleDownloadProgress(PacketDownloadProgress packet);

	public void shutdownMinecraft();
	
	public void displayRestartFailure();
	
	INetworkManager getNetworkManagerFromClient(NetHandler clientHandler);
	
}
