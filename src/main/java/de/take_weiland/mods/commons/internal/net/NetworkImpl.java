package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.Players;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class NetworkImpl {

    static Map<String, PacketHandler> channels = new ConcurrentHashMap<>();

    private static final String MULTIPART_CHANNEL = "SevenCommons|MP";
    private static final int MULTIPART_PREFIX = 0;
    private static final int MULTIPART_DATA = 1;

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player, PartTracker tracker) throws IOException {
        String channelName = mcPacket.getChannel();
        PacketHandler handler = channels.get(channelName);
        if (handler != null) {
            handler.accept(mcPacket.getData(), player, Side.SERVER);
            return true;
        } else if (channelName.equals(MULTIPART_CHANNEL)) {
            handleMultipartPacket(mcPacket.getData(), tracker, player, Side.SERVER);
            return true;
        } else {
            return false;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket, PartTracker tracker) throws IOException {
        String channelName = mcPacket.func_149169_c();
        PacketHandler handler = channels.get(channelName);
        if (handler != null) {
            handler.accept(mcPacket.func_149168_d(), Players.getClient(), Side.CLIENT);
            return true;
        } else if (channelName.equals(MULTIPART_CHANNEL)) {
            handleMultipartPacket(mcPacket.func_149168_d(), tracker, Players.getClient(), Side.CLIENT);
            return true;
        } else {
            return false;
        }
    }

    private static void handleMultipartPacket(byte[] data, PartTracker tracker, EntityPlayer player, Side side) throws IOException {
        if (data[0] == MULTIPART_PREFIX) {
            MCDataInput in = Network.newInput(data, 1, data.length - 1);
            String channel = in.readString();
            int len = in.readVarInt();
            tracker.start(channel, len);
        } else {
            tracker.onPart(data);
            byte[] complete = tracker.checkDone();
            if (complete != null) {
                String channel = tracker.channel();
                try {
                    channels.get(channel).accept(complete, player, side);
                } catch (NullPointerException e) {
                    throw new IOException("Unknown channel in multipart packet " + channel);
                }
            }
        }
    }

    private static int positiveVarIntLen(int i) {
        // divide by 7 and round up see http://stackoverflow.com/a/7446742
        // actually ((32 - Integer.nOLZ(i)) + 6) / 7
        return Math.max(1, 38 - Integer.numberOfLeadingZeros(i) / 7);
    }

    static void writeMultipartPacket(String channel, byte[] data, ChannelHandlerContext ctx, ChannelPromise promise, int maxSize, BiFunction<String, byte[], ? extends net.minecraft.network.Packet> cstr) {
        int channelLen = channel.length();
        int dataLen = data.length;
        MCDataOutput out = Network.newOutput(1 + positiveVarIntLen(channelLen) + (channelLen << 1) + positiveVarIntLen(dataLen));
        out.writeByte(MULTIPART_PREFIX);
        out.writeString(channel);
        out.writeVarInt(dataLen);

        ctx.write(cstr.apply(MULTIPART_CHANNEL, out.backingArray()));

        maxSize--; // need one byte prefix

        int parts = (dataLen + (maxSize - 1)) / maxSize; // divide by maxSize and round up
        for (int i = 0; i < (parts - 1); i++) {
            byte[] dataThisPart = new byte[maxSize + 1];
            dataThisPart[0] = MULTIPART_DATA;
            System.arraycopy(data, i * maxSize, dataThisPart, 1, maxSize);
            ctx.write(cstr.apply(MULTIPART_CHANNEL, dataThisPart));
        }

        int leftover = dataLen - (parts - 1) * maxSize;
        byte[] dataThisPart = new byte[leftover + 1];
        dataThisPart[0] = MULTIPART_DATA;
        System.arraycopy(data, (parts - 1) * maxSize, dataThisPart, 1, leftover);
        ctx.write(cstr.apply(MULTIPART_CHANNEL, dataThisPart), promise);
    }

    public static byte[] encodePacket(Packet packet, SimplePacketData data) {
        MCDataOutput out = Network.newOutput(packet.expectedSize() + 1);
        out.writeByte(data.packetID);
        packet.writeTo(out);
        return out.toByteArray();
    }

    // registering

    public static synchronized void register(String channel, PacketHandler handler) {
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

    @SideOnly(Side.CLIENT)
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

    private NetworkImpl() {
    }
}
