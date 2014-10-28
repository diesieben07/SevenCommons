package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public interface SevenCommonsProxy {

	public void preInit(FMLPreInitializationEvent event);

	INetworkManager getNetworkManagerFromClient(NetHandler clientHandler);

	void sendPacketToServer(Packet p);

	EntityPlayer getClientPlayer();

	String translate(String key);

}
