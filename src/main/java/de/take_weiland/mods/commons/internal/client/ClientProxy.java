package de.take_weiland.mods.commons.internal.client;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.NetClientHandler;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public class ClientProxy implements SevenCommonsProxy {

	private final Minecraft mc = Minecraft.getMinecraft();

	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		return ((NetClientHandler) clientHandler).getNetManager();
	}

	@Override
	public void sendPacketToServer(Packet p) {
		mc.getNetHandler().addToSendQueue(p);
	}

}
