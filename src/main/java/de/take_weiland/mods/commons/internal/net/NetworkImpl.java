package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.net.ChannelHandler;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.channel.*;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class NetworkImpl {

    public static final Logger LOGGER = SevenCommons.scLogger("Network");

    static Map<String, ChannelHandler> channels = new ConcurrentHashMap<>();

    private static final String MULTIPART_CHANNEL = "SevenCommons|MP";
    private static final int MULTIPART_PREFIX = 0;
    private static final int MULTIPART_DATA = 1;

    static void invokeHandler(ChannelHandler handler, String channel, byte[] data, EntityPlayer player, byte side) {
        byte c = handler.characteristics();
        if ((c & side) == 0) {
            throw new ProtocolException(String.format("Received packet on channel %s with handler %s on invalid side %s", channel, handler, side == RawPacket.CLIENT ? "client" : "server"));
        }
        if ((c & RawPacket.ASYNC) == 0) {
            if (side == RawPacket.CLIENT) {
                SchedulerInternalTask.execute(Scheduler.client(), new SchedulerInternalTask() {
                    @Override
                    public boolean run() {
                        handler.accept(channel, data, Players.getClient(), Side.CLIENT);
                        return false;
                    }
                });
            } else {
                SchedulerInternalTask.execute(Scheduler.server(), new SchedulerInternalTask() {
                    @Override
                    public boolean run() {
                        handler.accept(channel, data, player, Side.SERVER);
                        return false;
                    }
                });
            }
        } else {
            handler.accept(channel, data, player, side == RawPacket.CLIENT ? Side.CLIENT : Side.SERVER);
        }
    }

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player, SCMessageHandler tracker) throws IOException {
        String channelName = mcPacket.getChannel();
        ChannelHandler handler = channels.get(channelName);
        if (handler != null) {
            handler.accept(channelName, mcPacket.getData(), player, Side.SERVER);
            return true;
        } else if (channelName.equals(MULTIPART_CHANNEL)) {
            handleMultipartPacket(mcPacket.getData(), tracker, player, Side.SERVER);
            return true;
        } else {
            return false;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket, SCMessageHandler tracker) throws IOException {
        String channelName = mcPacket.func_149169_c();
        ChannelHandler handler = channels.get(channelName);
        if (handler != null) {
            handler.accept(channelName, mcPacket.func_149168_d(), Players.getClient(), Side.CLIENT);
            return true;
        } else if (channelName.equals(MULTIPART_CHANNEL)) {
            handleMultipartPacket(mcPacket.func_149168_d(), tracker, Players.getClient(), Side.CLIENT);
            return true;
        } else {
            return false;
        }
    }

    private static void handleMultipartPacket(byte[] data, SCMessageHandler tracker, EntityPlayer player, Side side) throws IOException {
        if (data[0] == MULTIPART_PREFIX) {
            MCDataInput in = Network.newInput(data, 1, data.length - 1);
            String channel = in.readString();
            int len = in.readVarInt();
            if (tracker.multipartChannel != null) {
                throw new IOException("New Multipart packet before old was finished");
            }
            tracker.multipartChannel = channel;
            tracker.multipartData = new byte[len];
            tracker.multipartPos = 0;
        } else {
            int actLen = data.length - 1;

            System.arraycopy(data, 1, tracker.multipartData, tracker.multipartPos, actLen);
            tracker.multipartPos += actLen;
            byte[] result;
            if (tracker.multipartPos == tracker.multipartData.length) {
                result = tracker.multipartData;
            } else {
                result = null;
            }
            byte[] complete = result;
            if (complete != null) {
                String channel = tracker.multipartChannel;
                tracker.multipartData = null;
                tracker.multipartChannel = null;
                try {
                    channels.get(channel).accept(channel, complete, player, side);
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

    public static synchronized void register(String channel, ChannelHandler handler) {
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

    private static void insertHandler(ChannelPipeline pipeline, io.netty.channel.ChannelHandler handler) {
        pipeline.addBefore("packet_handler", "sevencommons:handler", handler);
    }

    private static void insertEncoder(ChannelPipeline pipeline, io.netty.channel.ChannelHandler encoder) {
        // this is "backwards" - outbound messages travel "upwards" in the pipeline
        // so really the order is sevencommons:encoder and then vanilla's encoder
        pipeline.addAfter("encoder", "sevencommons:encoder", encoder);
    }

    private NetworkImpl() {
    }

    public static void sendTo(Iterable<? extends EntityPlayer> players, BaseNettyPacket packet) {
        for (EntityPlayer player : players) {
            sendToPlayer(Players.checkNotClient(player), packet);
        }
    }

    public static void sendToPlayer(EntityPlayerMP player, BaseNettyPacket packet) {
        player.playerNetServerHandler.netManager.channel()
                .writeAndFlush(packet)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToServer(BaseNettyPacket packet) {
        SevenCommons.proxy.getClientNetworkManager().channel()
                .writeAndFlush(packet)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
