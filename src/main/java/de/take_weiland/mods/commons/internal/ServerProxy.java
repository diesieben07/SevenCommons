package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet;

public class ServerProxy implements SevenCommonsProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
	}

	@Override
	public INetworkManager getNetworkManagerFromClient(NetHandler clientHandler) {
		throw new IllegalStateException("NetHandler.isServerHandler() should always be true on a dedicated server!");
	}

	@Override
	public void sendPacketToServer(Packet p) {
		throw new IllegalStateException("Server cannot send Packet to itself!");
	}

	@Override
	public EntityPlayer getClientPlayer() {
		throw new IllegalStateException("Server has no client player!");
	}

	@Override
	public String translate(String key) {
		throw new IllegalStateException("Tried to translate on the server, use ChatMessageComponent or translate on the client");
	}
}
