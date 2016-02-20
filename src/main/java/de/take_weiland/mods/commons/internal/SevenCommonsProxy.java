package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

public interface SevenCommonsProxy {

    default void preInit(FMLPreInitializationEvent event) {
    }

    void sendPacketToServer(Packet p);

    EntityPlayer getClientPlayer();

    NetworkManager getClientNetworkManager();

    Packet makeC17Packet(String channel, byte[] data);

}
