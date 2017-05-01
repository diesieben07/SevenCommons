package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ServerProxy implements SevenCommonsProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Override
    public void sendPacketToServer(Packet<INetHandlerPlayServer> p) {
        throw new IllegalStateException("Server cannot send Packet to itself!");
    }

    @NotNull
    @Override
    public EntityPlayer getClientPlayer() {
        throw new IllegalStateException("Server has no client player!");
    }

    @Nonnull
    @Override
    public World getClientWorld() {
        throw new IllegalStateException("Server has no client world");
    }

    @Override
    public NetworkManager getClientNetworkManager() {
        throw new IllegalStateException("Tried to send packet to the server on the server!");
    }

    @Override
    public Packet<INetHandlerPlayServer> newServerboundPacket(String channel, PacketBuffer payload) {
        throw new IllegalStateException("Tried to encode into serverbound packet on the server");
    }

}
