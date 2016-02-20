package de.take_weiland.mods.commons.internal.client;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static net.minecraft.client.Minecraft.getMinecraft;

public final class ClientProxy implements SevenCommonsProxy {

    @Override
    public void sendPacketToServer(Packet p) {
        getMinecraft().getNetHandler().addToSendQueue(p);
    }

    @Override
    public EntityPlayer getClientPlayer() {
        return getMinecraft().thePlayer;
    }

    @Override
    public NetworkManager getClientNetworkManager() {
        return getMinecraft().getNetHandler().getNetworkManager();
    }

    @Override
    public Packet makeC17Packet(String channel, byte[] data) {
        return new C17PacketCustomPayload(channel, data);
    }

    public static final MethodHandle netHandlerClientWorldSet;

    static {
        try {
            Field field = NetHandlerPlayClient.class.getDeclaredField(MCPNames.field(SRGConstants.M_NET_HANDLER_PLAY_CLIENT_WORLD));
            field.setAccessible(true);
            netHandlerClientWorldSet = MethodHandles.publicLookup().unreflectSetter(field);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

}
