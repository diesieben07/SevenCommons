package de.take_weiland.mods.commons.netx;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.network.NetworkRegistry;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.map.TByteObjectMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class NetworkImpl {

    private static Map<String, TByteObjectMap<MessageType<?>>> serverChannels = new ConcurrentHashMap<>();
    private static Map<String, TByteObjectMap<MessageType<?>>> clientChannels = new ConcurrentHashMap<>();

    // receiving

    static boolean handleServerCustomPacket(C17PacketCustomPayload mcPacket, EntityPlayerMP player) throws IOException {
        String channel = mcPacket.func_149559_c();
        TByteObjectMap<MessageType<?>> handlers = serverChannels.get(channel);
        if (handlers == null) {
            return false;
        } else {
            doHandle(mcPacket.func_149558_e(), channel, player, handlers);
            return true;
        }
    }

    static boolean handleClientCustomPacket(S3FPacketCustomPayload mcPacket) throws IOException {
        String channel = mcPacket.func_149169_c();
        TByteObjectMap<MessageType<?>> handlers = clientChannels.get(channel);
        if (handlers == null) {
            return false;
        } else {
            doHandle(mcPacket.func_149168_d(), channel, Players.getClient(), handlers);
            return true;
        }
    }

    private static void doHandle(byte[] payload, String channel, EntityPlayer player, TByteObjectMap<MessageType<?>> handlers) throws IOException {
        ByteBuf buf = Unpooled.wrappedBuffer(payload);
        byte id = buf.readByte();
        MessageType<?> handler = handlers.get(id);
        if (handler == null) {
            throw new IOException(String.format("Received unknown ID %d on channel %s", id, channel));
        }

        handler.decodeAndHandle(buf, player);
    }

    // sending

    static void sendToServer(String channel, int id, Object object) {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();

        if (netHandler.getNetworkManager().isLocalChannel()) {
            MessageType<?> message = serverChannels.get(channel).get((byte) id);
            //noinspection unchecked,rawtypes
            Scheduler.server().execute(() -> ((BiConsumer) message.handler).accept(object, Players.getSPOwner()));
        } else {
            netHandler.addToSendQueue(serverboundPacket(channel, id, object));
        }
    }

    static void sendToClient(String channel, int id, Object object, EntityPlayerMP player) {
        if (player.playerNetServerHandler.netManager.isLocalChannel()) {
            sendToClientLocal(channel, id, object);
        } else {
            player.playerNetServerHandler.sendPacket(clientboundPacket(channel, id, object));
        }
    }

    private static void sendToClientLocal(String channel, int id, Object object) {
        MessageType<?> message = clientChannels.get(channel).get((byte) id);
        //noinspection unchecked,rawtypes
        Scheduler.client().execute(() -> ((BiConsumer) message.handler).accept(object, Players.getClient()));
    }

    static void sendToClients(String channel, int id, Object object, Iterator<EntityPlayerMP> players) {
        while (players.hasNext()) {
            EntityPlayerMP next = players.next();
            if (next.playerNetServerHandler.netManager.isLocalChannel()) {
                sendToClientLocal(channel, id, object);
            } else {
                sendToClientsRest(next, channel, id, object, players);
                break;
            }
        }
    }

    private static void sendToClientsRest(EntityPlayerMP player, String channel, int id, Object object, Iterator<EntityPlayerMP> players) {
        net.minecraft.network.Packet packet = clientboundPacket(channel, id, object);

        player.playerNetServerHandler.sendPacket(packet);

        while (players.hasNext()) {
            player = players.next();
            if (player.playerNetServerHandler.netManager.isLocalChannel()) {
                sendToClientLocal(channel, id, object);
            } else {
                player.playerNetServerHandler.sendPacket(packet);
            }
        }
    }

    private static S3FPacketCustomPayload clientboundPacket(String channel, int id, Object object) {
        return new S3FPacketCustomPayload(channel, makeBuf(clientChannels, channel, id, object));
    }

    private static C17PacketCustomPayload serverboundPacket(String channel, int id, Object object) {
        return new C17PacketCustomPayload(channel, makeBuf(serverChannels, channel, id, object));
    }

    @SuppressWarnings({"unchecked", "rawtypes"}) // caller needs to make sure this is safe!
    private static ByteBuf makeBuf(Map<String, TByteObjectMap<MessageType<?>>> channels, String channel, int id, Object object) {
        TByteObjectMap<MessageType<?>> packets = channels.get(channel);
        MessageType<?> type = packets.get((byte) id);

        ByteBuf buf = Unpooled.buffer(((ToIntFunction) type.sizeEstimate).applyAsInt(object) + 1);
        ((BiConsumer) type.encoder).accept(object, buf);

        return buf;
    }

    // registering

    static synchronized void register(String channel, TByteObjectMap<MessageType<?>> clientHandlers, TByteObjectMap<MessageType<?>> serverHandlers) {
        NetworkRegistry.INSTANCE.newChannel(channel);

        checkNotFrozen();

        // we only allow to put into clientChannels and serverChannels at once
        // so checking one is enough
        if (clientChannels.putIfAbsent(channel, clientHandlers) != null) {
            throw new IllegalStateException(String.format("Channel %s is already taken", channel));
        }
        serverChannels.put(channel, serverHandlers);
    }

    private static synchronized void freeze() {
        clientChannels = ImmutableMap.copyOf(clientChannels);
        serverChannels = ImmutableMap.copyOf(serverChannels);
    }

    private static void checkNotFrozen() {
        checkState(clientChannels instanceof ImmutableMap, "Must register packets before postInit");
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, NetworkImpl::freeze);
    }

}
