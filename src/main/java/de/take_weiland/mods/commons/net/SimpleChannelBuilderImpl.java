package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private final TByteObjectHashMap<Function<? super MCDataInput, ? extends BaseModPacket>> constructors = new TByteObjectHashMap<>();
    private final Map<Class<? extends Packet>, HandlerIDPair> handlers = new HashMap<>();

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
        handlers.put(packetClass, new HandlerIDPair((BiConsumer<? super Packet, ? super EntityPlayer>) handler, (byte) id));

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> respCstr, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        return register(id, constructor, (packet, player) -> {

        });
    }

    @Override
    public void build() {
        checkNotBuilt();
        ImmutableMap<Class<? extends Packet>, HandlerIDPair> handlers = ImmutableMap.copyOf(this.handlers);

        Function<? super MCDataInput, ? extends Packet>[] packedCstrs = pack(constructors);

        SimplePacketCodec codec = new SimplePacketCodec(channel, packedCstrs, handlers);
        // use the original keySet, avoid having the ImmutableMap keep it around
        PacketToChannelMap.putAll(this.handlers.keySet(), codec);
        NetworkImpl.register(channel, codec);
    }

    private static Function<? super MCDataInput, ? extends Packet>[] pack(TByteObjectMap<Function<? super MCDataInput, ? extends Packet>> map) {
        int maxId = 0;
        for (byte b : map.keys()) {
            int asInt = UnsignedBytes.toInt(b);
            if (asInt > maxId) {
                maxId = asInt;
            }
        }
        @SuppressWarnings("unchecked")
        Function<? super MCDataInput, ? extends Packet>[] arr = new Function[maxId + 1];
        TByteObjectIterator<Function<? super MCDataInput, ? extends Packet>> it = map.iterator();
        while (it.hasNext()) {
            it.advance();
            arr[UnsignedBytes.toInt(it.key())] = it.value();
        }
        return arr;
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

}
