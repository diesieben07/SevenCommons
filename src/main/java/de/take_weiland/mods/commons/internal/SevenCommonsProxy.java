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

public interface SevenCommonsProxy {

    default void preInit(FMLPreInitializationEvent event) {
    }

    void sendPacketToServer(Packet<INetHandlerPlayServer> p);

    @Nonnull
    EntityPlayer getClientPlayer();

    @Nonnull
    World getClientWorld();

    NetworkManager getClientNetworkManager();

    Packet<INetHandlerPlayServer> newServerboundPacket(String channel, PacketBuffer payload);

}
