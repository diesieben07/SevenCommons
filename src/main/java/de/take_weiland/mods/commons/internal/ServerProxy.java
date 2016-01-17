package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import de.take_weiland.mods.commons.internal.worldview.PacketBlockChange;
import de.take_weiland.mods.commons.internal.worldview.PacketChunkData;
import de.take_weiland.mods.commons.internal.worldview.PacketChunkUnload;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

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

    @Override
    public void handleChunkDataPacket(PacketChunkData packet, EntityPlayer player) {
        throw new IllegalStateException("Cannot handle chunk data packet on the server");
    }

    @Override
    public void handleChunkUnloadPacket(PacketChunkUnload packet, EntityPlayer player) {
        throw new IllegalStateException("Cannot handle chunk unload packet on the server");
    }

    @Override
    public void handleBlockChangePacket(PacketBlockChange packet, EntityPlayer player) {
        throw new IllegalStateException("Cannot handle block change packet on the server");
    }
}
