package de.take_weiland.mods.commons.internal.client;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.SevenCommonsProxy;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import static net.minecraft.client.Minecraft.getMinecraft;

public final class ClientProxy implements SevenCommonsProxy {

    @Override
    public void sendPacketToServer(Packet<INetHandlerPlayServer> p) {
        getMinecraft().getConnection().sendPacket(p);
    }

    @Nonnull
    @Override
    public EntityPlayer getClientPlayer() {
        return getMinecraft().player;
    }

    @Nonnull
    @Override
    public World getClientWorld() {
        return getMinecraft().world;
    }

    @Override
    public NetworkManager getClientNetworkManager() {
        return getMinecraft().getConnection().getNetworkManager();
    }

    @Override
    public Packet<INetHandlerPlayServer> newServerboundPacket(String channel, PacketBuffer payload) {
        return new CPacketCustomPayload(channel, new PacketBuffer(Unpooled.wrappedBuffer(payload))); // todo
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
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
