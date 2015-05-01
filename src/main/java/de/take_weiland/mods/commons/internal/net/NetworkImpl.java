package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.PacketCodec;
import de.take_weiland.mods.commons.util.Players;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkState;

/**
 *
 *
 * @author diesieben07
 */
public final class NetworkImpl {

    public static final ChannelHandler TO_SERVER_ENCODER = new ChannelOutboundHandlerAdapter() {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof PacketCodecPair) {
                ctx.write(((PacketCodecPair<?>) msg).makeServerboundMCPacket(), promise);
            } else {
                ctx.write(msg, promise);
            }
        }
    };

    public static final ChannelHandler TO_CLIENT_ENCODER = new ChannelOutboundHandlerAdapter() {

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof PacketCodecPair) {
                ctx.write(((PacketCodecPair<?>) msg).makeClientboundMCPacket(), promise);
            } else {
                ctx.write(msg, promise);
            }
        }
    };

    private static Map<String, PacketCodec<?>> channels = new ConcurrentHashMap<>();

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player) throws IOException {
        String channelName = mcPacket.func_149559_c();
        PacketCodec<?> codec = channels.get(channelName);
        if (codec == null) {
            return false;
        } else {
            decodeAndHandle(codec, mcPacket.func_149558_e(), player);
            return true;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket) throws IOException {
        String channelName = mcPacket.func_149169_c();
        PacketCodec<?> codec = channels.get(channelName);
        if (codec == null) {
            return false;
        } else {
            decodeAndHandle(codec, mcPacket.func_149168_d(), Players.getClient());
            return true;
        }
    }

    static <P> void decodeAndHandle(PacketCodec<P> codec, byte[] payload, EntityPlayer player) {
        codec.handle(codec.decode(Unpooled.wrappedBuffer(payload)), player);
    }

    // registering

    public static synchronized <P> PacketCodec<P> register(String channel, PacketCodec<P> codec) {
        checkNotFrozen();
        if (channels.putIfAbsent(channel, codec) != null) {
            throw new IllegalStateException(String.format("Channel %s already registered", channel));
        }
        return codec;
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
