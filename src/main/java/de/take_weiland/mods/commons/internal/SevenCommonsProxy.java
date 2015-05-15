package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

public interface SevenCommonsProxy {

    void preInit(FMLPreInitializationEvent event);

    void sendPacketToServer(Packet p);

    EntityPlayer getClientPlayer();

    String translate(String key);

    NetworkManager getClientNetworkManager();

}
