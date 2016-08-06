package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.internal.BuilderLeakDetect;
import de.take_weiland.mods.commons.net.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * <ul>
 * <li>
 * Normal packets are sent as-is (Packet class extends InternalPacket).
 * <ul>
 * <li>Direct (local) handling goes via {@link Packet#_sc$internal$receiveDirect(byte, NetworkManager)} and then via the {@link PacketData}.</li>
 * <li>Writing to remote goes via {@link Packet#_sc$internal$writeTo(MCDataOutput)} which delegates to {@link Packet#writeTo(MCDataOutput)}.</li>
 * <li>Reading from remote goes via {@link ModPacketChannelHandler} which delegates to a {@link NormalHandler} based on packetID.</li>
 * </ul>
 * </li>
 * <li>
 * Packets with response are sent via {@link WrappedPacketWithResponse} which extends InternalPacket.
 * <ul>
 *     <li>Direct (local) handling goes via {@link WrappedPacketWithResponse#_sc$internal$receiveDirect(byte, NetworkManager)}
 *     and then via {@link PacketData} which provides the handler. The future is directly completed from there.</li>
 *     <li>Writing to remote goes via {@link WrappedPacketWithResponse#_sc$internal$writeTo(MCDataOutput)} which registers
 *     the waiting future with {@link ResponseSupport} to create a unique ID which is also written to the stream.</li>
 *     <li>Reading from remote goes via {@link ModPacketChannelHandler} which delegates to a {@link ResponseHandler}. The
 *     response is sent from there as a {@link WrappedResponsePacket}. This is then handled on the remote side in the same way,
 *     coming back to {@link ResponseHandler#accept(String, int, MCDataInput, byte, NetworkManager)}, which then
 *     completes the future.</li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author diesieben07
 */
public final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private final Runnable notifyWhenDone;
    private Map<Integer, Handler> handlers = new HashMap<>();

    public SimpleChannelBuilderImpl(String channel) {
        this.channel = channel;
        notifyWhenDone = BuilderLeakDetect.createBuilder(this, "SimpleChannelBuilder(" + channel + ')');
    }

    @Override
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketHandler<P> handler) {
        byte characteristics = getCharacteristics(handler);
        addHandler(id, new NormalHandler<>(characteristics, handler, constructor));
        addData(constructor, handler, characteristics, id);

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithResponse<P, R> handler) {
        byte characteristics = getCharacteristics(handler);
        addHandler(id, new ResponseNormalHandler<>(characteristics, constructor, responseConstructor, handler));
        addData(constructor, handler, characteristics, id);

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithAsyncResponse<P, R> handler) {
        byte characteristics = getCharacteristics(handler);
        addHandler(id, new ResponseFutureHandler<>(characteristics, constructor, responseConstructor, handler));
        addData(constructor, handler, characteristics, id);

        return this;
    }

    private void addHandler(int id, Handler handler) {
        checkNotBuilt();
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        if (handlers.putIfAbsent(id, handler) != null) {
            throw new IllegalArgumentException("Duplicate ID " + id);
        }
    }

    private void addData(PacketConstructor<? extends PacketWithData> constructor, PacketHandlerBase handler, byte characteristics, int id) {
        PacketRegistry.register(constructor.getPacketClass(), new PacketData(handler, characteristics, (byte) id, channel));
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

    @Override
    public void build() {
        checkNotBuilt();

        Handler[] handlersPacked = pack(handlers);

        handlers = null; // for already built detection

        NetworkImpl.register(channel, new ModPacketChannelHandler(handlersPacked));

        notifyWhenDone.run();
    }

    private static Handler[] pack(Map<Integer, Handler> map) {
        int maxID = Collections.max(map.keySet());

        Handler[] arr = new Handler[maxID + 1];
        for (Map.Entry<Integer, Handler> entry : map.entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }

        return arr;
    }

    private static byte getCharacteristics(PacketHandlerBase packetHandler) {
        byte c = 0;
        if (packetHandler.isAsync()) {
            c |= Network.ASYNC;
        }
        Optional<Side> receivingSide = packetHandler.receivingSide();
        if (receivingSide.isPresent()) {
            c |= receivingSide.get().isClient() ? Network.CLIENT : Network.SERVER;
        } else {
            c |= Network.BIDIRECTIONAL;
        }
        return c;
    }

    private static final class ModPacketChannelHandler implements ChannelHandler {

        private final Handler[] handlers;

        ModPacketChannelHandler(Handler[] handlers) {
            this.handlers = handlers;
        }

        @Override
        public void accept(String channel, ByteBuf payload, byte side, NetworkManager manager) {
            MCDataInput in = Network.newInput(payload);
            int packetID = in.readUnsignedByte();
            Handler handler;
            if (packetID >= handlers.length || (handler = handlers[packetID]) == null) {
                throw unknownIDException(channel, packetID);
            }

            if ((handler.characteristics & side) == 0) {
                throw new ProtocolException("PacketID " + packetID + " received on invalid side " + (side == Network.CLIENT ? "client" : "server"));
            }

            handler.accept(channel, packetID, in, side, manager);
        }

        private static ProtocolException unknownIDException(String channel, int packetID) {
            return new ProtocolException(String.format("Unknown packetID %s in channel %s", packetID, channel));
        }

    }

    /**
     * <p>Base class for packetID specific handlers. channel and ID are passed here too so they don't need to be captured in
     * every handler object.</p>
     */
    abstract static class Handler {

        final byte characteristics;

        Handler(byte characteristics) {
            this.characteristics = characteristics;
        }

        public abstract void accept(String channel, int packetID, MCDataInput in, byte side, NetworkManager manager);

        protected static RuntimeException wrapConstructException(int packetID, Exception x) {
            return new RuntimeException(String.format("Exception while constructing packet %s", packetID), x);
        }

    }

}
