package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

import java.util.function.BiFunction;

public interface SevenCommonsProxy {

    void preInit(FMLPreInitializationEvent event);

    void sendPacketToServer(Packet p);

    EntityPlayer getClientPlayer();

    NetworkManager getClientNetworkManager();

    BiFunction<String, byte[], ? extends Packet> getC17PacketCstr();

    Packet makeC17Packet(String channel, byte[] data);
}
