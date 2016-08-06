package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface SevenCommonsProxy {

    default void preInit(FMLPreInitializationEvent event) {
    }

    void sendPacketToServer(Packet<INetHandlerPlayServer> p);

    EntityPlayer getClientPlayer();

    NetworkManager getClientNetworkManager();

    Packet<INetHandlerPlayServer> newServerboundPacket(String channel, PacketBuffer payload);

}
