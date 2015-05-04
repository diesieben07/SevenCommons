package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private TByteObjectHashMap<Function<? super MCDataInput, ? extends Packet>> constructors = new TByteObjectHashMap<>();
    private TObjectByteHashMap<Class<? extends Packet>> idLookup = new TObjectByteHashMap<>();
    private Map<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers = new HashMap<>();

    SimpleChannelBuilderImpl(String channel) {
        this.channel = channel;
    }

    @Override
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        checkArgument(!constructors.containsKey((byte) id), "ID already taken");

        Class<? extends Packet> packetClass = constructor.getPacketClass();
        checkArgument(!handlers.containsKey(packetClass), "Duplicate packet class");

        constructors.put((byte) id, constructor);
        //noinspection unchecked
        handlers.put(packetClass, (BiConsumer<? super Packet, ? super EntityPlayer>) handler);
        idLookup.put(packetClass, (byte) id);

        return this;
    }

    @Override
    public void build() {
        checkNotBuilt();
        TByteObjectHashMap<Function<? super MCDataInput, ? extends Packet>> constructors = this.constructors;
        constructors.compact();

        TObjectByteHashMap<Class<? extends Packet>> idLookup = this.idLookup;
        idLookup.compact();

        ImmutableMap<Class<? extends Packet>, BiConsumer<? super Packet, ? super EntityPlayer>> handlers = ImmutableMap.copyOf(this.handlers);

        SimplePacketCodec codec = new SimplePacketCodec(channel, constructors, handlers, idLookup);
        // use the original keySet, avoid having the ImmutableMap keep it around
        PacketToChannelMap.putAll(this.handlers.keySet(), codec);
        NetworkImpl.register(channel, codec);
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

}
