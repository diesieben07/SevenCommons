package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketCodec;
import de.take_weiland.mods.commons.net.SimplePacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author diesieben07
 */
public final class PacketToChannelMap {

    private static Map<Class<? extends BaseModPacket>, SimplePacketData<?>> channels = new ConcurrentHashMap<>();

    public static <P extends BaseModPacket> SimplePacketData<P> getData(P packet) {
        SimplePacketData<?> data = channels.get(packet.getClass());
        if (data == null) {
            throw new IllegalStateException(String.format("Cannot send unregistered Packet %s", packet.getClass().getName()));
        }
        //noinspection unchecked
        return (SimplePacketData<P>) data;
    }

    static synchronized void put(Class<? extends Packet> packetClass, PacketCodec<Packet> codec) {
        PacketCodec<Packet> oldCodec = channels.putIfAbsent(packetClass, codec);
        if (oldCodec != null) {
            throw new IllegalStateException(String.format("Packet %s already in use with channel %s", packetClass.getName(), oldCodec.channel()));
        }
    }

    public static synchronized void register(Class<? extends BaseModPacket> clazz, String channel, int id) {
        channels.putIfAbsent(clazz, new SimplePacketData<>())
    }

    public static synchronized void putAll(Iterable<Class<? extends Packet>> packets, PacketCodec<Packet> codec) {
        for (Class<? extends Packet> packet : packets) {
            PacketCodec<Packet> oldCodec = channels.putIfAbsent(packet, codec);
            if (oldCodec != null) {
                throw new IllegalStateException(String.format("Packet %s already in use with channel %s", packet.getName(), oldCodec.channel()));
            }
        }
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, PacketToChannelMap::freeze);
    }

    private PacketToChannelMap() {
    }

}
