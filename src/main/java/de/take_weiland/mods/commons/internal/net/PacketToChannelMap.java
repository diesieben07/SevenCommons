package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.ImmutableMap;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.NetworkChannel;
import de.take_weiland.mods.commons.net.Packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author diesieben07
 */
public final class PacketToChannelMap {

    private static Map<Class<? extends Packet>, NetworkChannelImpl<Packet>> channels = new ConcurrentHashMap<>();

    public static NetworkChannel<Packet> getChannel(Packet packet) {
        NetworkChannelImpl<Packet> channel = channels.get(packet.getClass());
        if (channel == null) {
            throw new IllegalStateException(String.format("Cannot send unregistered Packet %s", packet.getClass().getName()));
        }
        return channel;
    }

    static synchronized void put(Class<? extends Packet> packetClass, NetworkChannelImpl<Packet> channel) {
        NetworkChannelImpl<Packet> oldChannel = channels.putIfAbsent(packetClass, channel);
        if (oldChannel != null) {
            throw new IllegalStateException(String.format("Packet %s already in use with channel %s", packetClass.getName(), channel.channel));
        }
    }

    private static synchronized void freeze() {
        channels = ImmutableMap.copyOf(channels);
    }

    static {
        SevenCommons.registerStateCallback(LoaderState.ModState.POSTINITIALIZED, PacketToChannelMap::freeze);
    }

}
