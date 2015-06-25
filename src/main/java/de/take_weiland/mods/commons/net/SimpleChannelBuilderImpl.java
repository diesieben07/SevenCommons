package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * @author diesieben07
 */
final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private final TByteObjectHashMap<BiConsumer<? super MCDataInput, ? super EntityPlayer>> handlers = new TByteObjectHashMap<>();

    SimpleChannelBuilderImpl(String channel) {
        this.channel = channel;
    }

    @Override
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        Class<? extends BaseModPacket> packetClass = constructor.getPacketClass();
        validate(id, packetClass);

        handlers.put((byte) id, (in, player) -> {
            P packet = constructor.apply(in);
            Scheduler.forSide(sideOf(player)).execute(() -> handler.accept(packet, player));
        });
        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> respCstr, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        Class<P> packetClass = constructor.getPacketClass();
        Class<R> responseClass = respCstr.getPacketClass();

        validate(id, packetClass);
        checkDuplicateClass(responseClass);

        AtomicInteger nextID = new AtomicInteger();
        ConcurrentMap<Integer, CompletableFuture<R>> map = new MapMaker().concurrencyLevel(2).makeMap();
        String channelFinal = channel;

        handlers.put((byte) id, (in, player) -> {
            int uniqueID = in.readInt();
            if (uniqueID == -1) {
                CompletableFuture<R> future = new CompletableFuture<>();

                P packet = constructor.apply(in);
                R response = handler.apply(packet, player);


                BaseNettyPacket responseWrap = new BaseNettyPacket.UsingCustomPayload() {
                    @Override
                    public byte[] _sc$write() {
                        int responseID = nextID.getAndIncrement();
                        map.put(responseID, future);

                        MCDataOutput out = Network.newOutput(response.expectedSize() + 5);
                        out.writeByte(id);
                        out.writeInt(responseID);

                        response.writeTo(out);
                        return out.toByteArray();
                    }

                    @Override
                    public String _sc$channel() {
                        return channelFinal;
                    }

                    @Override
                    public void _sc$handle(EntityPlayer player) {
                        try {
                            future.complete(response);
                        } catch (Throwable t) {
                            future.completeExceptionally(t);
                        }
                    }
                };
                if (sideOf(player).isClient()) {
                    Network.sendToServer(responseWrap);
                } else {
                    Network.sendToPlayer((EntityPlayerMP) player, responseWrap);
                }
            } else {
                CompletableFuture<R> future = map.get(uniqueID);
                if (future != null) {
                    try {
                        future.complete(respCstr.apply(in));
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                }
            }
        });

        return this;
    }

    private void validate(int id, Class<? extends BaseModPacket> clazz) {
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        checkArgument(!constructors.containsKey((byte) id), "ID already taken");
        checkDuplicateClass(clazz);
    }

    private void checkDuplicateClass(Class<? extends BaseModPacket> clazz) {
        checkArgument(!handlers.containsKey(clazz), "Duplicate packet class %s", clazz.getName());
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
