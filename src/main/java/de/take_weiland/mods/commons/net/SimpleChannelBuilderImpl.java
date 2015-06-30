package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.concurrent.CompletableFuture;
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
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        Class<P> packetClass = constructor.getPacketClass();
        validate(id);
        PacketToChannelMap.register(packetClass, channel, id, handler);

        handlers.put((byte) id, (in, player) -> {
            P packet = constructor.apply(in, player);
            Scheduler.forSide(sideOf(player)).execute(() -> handler.accept(packet, player));
        });
        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        validate(id);

        Class<P> packetClass = constructor.getPacketClass();

        String channelFinal = channel;

        handlers.put((byte) id, (in, player) -> {
            int uniqueID = in.readInt();

            if (ResponseSupport.isResponse(uniqueID)) {
                @SuppressWarnings("unchecked")
                CompletableFuture<R> future = (CompletableFuture<R>) ResponseSupport.get(uniqueID);

                if (future != null) {
                    try {
                        future.complete(responseConstructor.apply(in, player));
                    } catch (Throwable t) {
                        future.completeExceptionally(t);
                    }
                }
            } else {
                P packet = constructor.apply(in, player);
                R response = handler.apply(packet, player);
                BaseNettyPacket responseWrap = new RawPacket.UsingCustomPayload() {

                    @Override
                    public byte[] write() {
                        MCDataOutput out = Network.newOutput(response.expectedSize() + 5);
                        out.writeByte(id);
                        out.writeInt(ResponseSupport.toResponse(uniqueID));
                        response.writeTo(out);
                        return out.toByteArray();
                    }

                    @Override
                    public void handle(EntityPlayer player) {
                        // this should never happen
                        // if we are on a local, direct connection the response itself should be handling
                        // see ResponseNettyVersion
                        throw new AssertionError();
                    }

                    @Override
                    public String channel() {
                        return channelFinal;
                    }
                };

                if (sideOf(player).isClient()) {
                    Network.sendToServer(responseWrap);
                } else {
                    Network.sendToPlayer((EntityPlayerMP) player, responseWrap);
                }
            }
        });

        PacketToChannelMap.register(packetClass, channel, id, handler);

        return this;
    }

    private void validate(int id) {
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
    }

    @Override
    public void build() {
        checkNotBuilt();
        handlers.compact();

        // do not capture "this" in lambda, for GC
        TByteObjectHashMap<BiConsumer<? super MCDataInput, ? super EntityPlayer>> handlers = this.handlers;
        String channel = this.channel;

        Network.registerHandler(this.channel, (payload, player) -> {
            MCDataInput in = Network.newInput(payload);
            byte id = in.readByte();
            BiConsumer<? super MCDataInput, ? super EntityPlayer> handler = handlers.get(id);
            if (handler == null) {
                throw new ProtocolException(String.format("Unknown packetID %s in channel %s", UnsignedBytes.toInt(id), channel));
            }
            handler.accept(in, player);
        });
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
