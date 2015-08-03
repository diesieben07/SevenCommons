package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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

        handlers.put((byte) id, createResponseHandler(id, constructor, responseConstructor, handler, channel));
        PacketToChannelMap.register(packetClass, channel, id, handler);

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, SimplePacketHandler.WithFutureResponse<? super P, ? extends R> handler) {
        validate(id);

        handlers.put((byte) id, createResponseFutureHandler(id, constructor, responseConstructor, handler, channel));

        return null;
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
            Scheduler.forSide(sideOf(player)).execute(() -> handler.accept(in, player));
        });
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

    private static <P extends Packet.WithResponse<R>, R extends Packet.Response> BiConsumer<MCDataInput, EntityPlayer> createResponseHandler(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler, String channel) {
        return (in, player) -> {
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
                BaseNettyPacket responseWrap = new ResponseWrapper<>(response, id, uniqueID, channel);

                if (sideOf(player).isClient()) {
                    Network.sendToServer(responseWrap);
                } else {
                    Network.sendToPlayer((EntityPlayerMP) player, responseWrap);
                }
            }
        };
    }

    private static <P extends Packet.WithResponse<R>, R extends Packet.Response> BiConsumer<? super MCDataInput, ? super EntityPlayer> createResponseFutureHandler(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler, String channel) {
        return (in, player) -> {
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
                CompletionStage<? extends R> responseFuture = handler.apply(packet, player);
                responseFuture.thenAcceptAsync(response -> {
                    BaseNettyPacket responseWrap = new ResponseWrapper<>(response, id, uniqueID, channel);

                    if (sideOf(player).isClient()) {
                        Network.sendToServer(responseWrap);
                    } else {
                        Network.sendToPlayer((EntityPlayerMP) player, responseWrap);
                    }
                });
            }
        };
    }

    private static class ResponseWrapper<R extends Packet.Response> implements BaseNettyPacket {

        private final R response;
        private final int id;
        private final int uniqueID;
        private final String channel;

        ResponseWrapper(R response, int id, int uniqueID, String channel) {
            this.response = response;
            this.id = id;
            this.uniqueID = uniqueID;
            this.channel = channel;
        }

        @Override
        public byte[] _sc$encode() {
            MCDataOutput out = Network.newOutput(response.expectedSize() + 5);
            out.writeByte(id);
            out.writeInt(ResponseSupport.toResponse(uniqueID));
            response.writeTo(out);
            return out.toByteArray();
        }

        @Override
        public void _sc$handle(EntityPlayer player) {
            // this should never happen
            // if we are on a local, direct connection the response itself should be handling
            // see ResponseNettyVersion
            throw new AssertionError();
        }

        @Override
        public String _sc$channel() {
            return channel;
        }
    }
}
