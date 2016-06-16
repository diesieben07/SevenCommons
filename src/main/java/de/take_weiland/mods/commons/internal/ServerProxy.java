package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ServerProxy implements SevenCommonsProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
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
    public NetworkManager getClientNetworkManager() {
        throw new IllegalStateException("Tried to send packet to the server on the server!");
    }

    @Override
    public Packet makeC17Packet(String channel, byte[] data) {
        throw new IllegalStateException("Tried to encode into serverbound packet on the server");
    }

}
