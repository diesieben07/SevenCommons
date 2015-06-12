package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class NetworkImpl {

    static Map<String, BiConsumer<? super byte[], ? super EntityPlayer>> channels = new ConcurrentHashMap<>();

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player) throws IOException {
        String channelName = mcPacket.func_149559_c();
        BiConsumer<? super byte[], ? super EntityPlayer> handler = channels.get(channelName);
        if (handler == null) {
            return false;
        } else {
            handler.accept(mcPacket.func_149558_e(), player);
            return true;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket) throws IOException {
        String channelName = mcPacket.func_149169_c();
        BiConsumer<? super byte[], ? super EntityPlayer> handler = channels.get(channelName);
        if (handler == null) {
            return false;
        } else {
            handler.accept(mcPacket.func_149168_d(), Players.getClient());
            return true;
        }
    }

    // registering

    public static synchronized void register(String channel, BiConsumer<? super byte[], ? super EntityPlayer> handler) {
        checkNotFrozen();
        if (channels.putIfAbsent(channel, handler) != null) {
            throw new IllegalStateException(String.format("Channel %s already registered", channel));
        }
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    private static void checkNotFrozen() {
        checkState(!(channels instanceof ImmutableMap), "Must register packets before postInit");
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, NetworkImpl::freeze);
    }

    public static void handleServersideConnection(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        NetHandlerPlayServer handler = (NetHandlerPlayServer) event.handler;
        ChannelPipeline pipeline = handler.netManager.channel().pipeline();

        if (!event.isLocal) {
            insertEncoder(pipeline, new ToClientEncoder(handler.playerEntity));
        }

        insertHandler(pipeline, new SCMessageHandlerServer(handler.playerEntity));
    }

    public static void handleClientsideConnection(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        NetHandlerPlayClient handler = (NetHandlerPlayClient) event.handler;
        ChannelPipeline pipeline = handler.getNetworkManager().channel().pipeline();

        if (!event.isLocal) {
            // only need the encoder when not connected locally
            insertEncoder(pipeline, ToServerEncoder.INSTANCE);
        }
        // handler handles both direct messages (for local) and the vanilla-payload packet
        insertHandler(pipeline, SCMessageHandlerClient.INSTANCE);
    }

    private static void insertHandler(ChannelPipeline pipeline, ChannelHandler handler) {
        pipeline.addBefore("packet_handler", "sevencommons:handler", handler);
    }

    private static void insertEncoder(ChannelPipeline pipeline, ChannelHandler encoder) {
        // this is "backwards" - outbound messages travel "upwards" in the pipeline
        // so really the order is sevencommons:encoder and then vanilla's encoder
        pipeline.addAfter("encoder", "sevencommons:encoder", encoder);
    }
}
