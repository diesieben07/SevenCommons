package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.Players;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkState;

/**
 *
 *
 * @author diesieben07
 */
public final class NetworkImpl {

    private static Map<String, NetworkChannelImpl> channels = new ConcurrentHashMap<>();

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player) throws IOException {
        String channelName = mcPacket.func_149559_c();
        NetworkChannelImpl networkChannel = channels.get(channelName);
        if (networkChannel == null) {
            return false;
        } else {
            networkChannel.decodeAndHandle(Unpooled.wrappedBuffer(mcPacket.func_149558_e()), player);
            return true;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket) throws IOException {
        String channelName = mcPacket.func_149169_c();
        NetworkChannelImpl<?> networkChannel = channels.get(channelName);
        if (networkChannel == null) {
            return false;
        } else {
            networkChannel.decodeAndHandle(Unpooled.wrappedBuffer(mcPacket.func_149168_d()), Players.getClient());
            return true;
        }
    }

    // sending

    public static <P> void sendToServer(NetworkChannelImpl<P> channel, P packet) {
        SevenCommons.proxy.getClientNetworkManager().channel()
                .writeAndFlush(new MessageChannelPair<>(packet, channel))
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static <P> void sendToPlayer(NetworkChannelImpl<P> channel, P packet, EntityPlayer player) {
        sendToPlayer0(new MessageChannelPair<>(packet, channel), Players.checkNotClient(player));
    }

    public static <P> void sendToPlayers(NetworkChannelImpl<P> channel,
                                         P packet,
                                         Iterable<? extends EntityPlayer> players) {
        sendToPlayers(channel, packet, players.iterator());
    }

    public static <P> void sendToPlayers(NetworkChannelImpl<P> channel,
                                  P packet,
                                  Iterable<? extends EntityPlayer> players,
                                  Predicate<? super EntityPlayerMP> filter) {
        sendToPlayers(channel, packet, players.iterator(), filter);
    }

    public static <P> void sendToPlayers(NetworkChannelImpl<P> channel,
                                  P packet,
                                  Iterator<? extends EntityPlayer> players) {
        sendToPlayers(channel, packet, players, player -> true);
    }

    public static <P> void sendToPlayers(NetworkChannelImpl<P> channel,
                                  P packet,
                                  Iterator<? extends EntityPlayer> players,
                                  Predicate<? super EntityPlayerMP> filter) {
        MessageChannelPair<P> pair = new MessageChannelPair<>(packet, channel);

        while (players.hasNext()) {
            EntityPlayerMP player = Players.checkNotClient(players.next());
            if (!filter.test(player)) continue;

            sendToPlayer0(pair, player);
        }
    }

    private static <P> void sendToPlayer0(MessageChannelPair<P> pair, EntityPlayerMP player) {
        player.playerNetServerHandler.netManager.channel()
                .writeAndFlush(pair)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    // registering

    public static synchronized <P> NetworkChannelImpl<P> register(String channel, Function<? super P, ? extends ByteBuf> encoder,
                                                           Function<? super ByteBuf, ? extends P> decoder,
                                                           BiConsumer<? super P, ? super EntityPlayer> handler) {
        checkNotFrozen();
        NetworkChannelImpl<P> channelImpl = new NetworkChannelImpl<>(channel, encoder, decoder, handler);
        channels.put(channel, channelImpl);
        return channelImpl;
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    private static void checkNotFrozen() {
        checkState(channels instanceof ImmutableMap, "Must register packets before postInit");
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, NetworkImpl::freeze);
    }

}
