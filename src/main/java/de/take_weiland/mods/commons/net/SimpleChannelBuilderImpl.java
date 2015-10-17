package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
final class SimpleChannelBuilderImpl implements SimpleChannelBuilder {

    private final String channel;
    private final TByteObjectHashMap<Handler> handlers = new TByteObjectHashMap<>();

    SimpleChannelBuilderImpl(String channel) {
        this.channel = channel;
    }

    @Override
    public <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        validateID(id);

        Class<P> packetClass = constructor.getPacketClass();
        byte info = getInfo(packetClass);

        PacketToChannelMap.register(packetClass, channel, id, info, handler);

        addHandler(id, new NormalHandler<>(info, handler, constructor));
        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        validateID(id);

        Class<P> packetClass = constructor.getPacketClass();
        byte info = getInfo(packetClass);

        PacketToChannelMap.register(packetClass, channel, id, info, handler);

        addHandler(id, new ResponseNormalHandler<>(info, constructor, responseConstructor, handler));

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerFutureResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler) {
        validateID(id);

        Class<P> packetClass = constructor.getPacketClass();
        byte info = getInfo(packetClass);

        PacketToChannelMap.registerFuture(packetClass, channel, id, info, handler);

        addHandler(id, new ResponseFutureHandler<>(info, constructor, responseConstructor, handler));

        return this;
    }

    private void addHandler(int id, Handler handler) {
        handlers.put(UnsignedBytes.checkedCast(id), handler);
    }

    private void validateID(int id) {
        checkArgument(id >= 0 && id <= 255, "ID must be 0-255");
        if (handlers.containsKey(UnsignedBytes.checkedCast(id))) {
            throw new IllegalArgumentException("Duplicate ID " + id);
        }
    }

    @Override
    public void build() {
        checkNotBuilt();

        Handler[] handlers = pack(this.handlers);
        String channel = this.channel;

        Network.registerHandler(this.channel, new SimpleChannelPacketHandler(handlers, channel));
    }

    private static class SimpleChannelPacketHandler implements ChannelHandler {
        private final Handler[] handlers;
        private final String channel;

        public SimpleChannelPacketHandler(Handler[] handlers, String channel) {
            this.handlers = handlers;
            this.channel = channel;
        }

        @Override
        public void accept(String channel, byte[] payload, EntityPlayer player, Side side) {
            MCDataInput in = Network.newInput(payload);
            int packetID = in.readUnsignedByte();
            Handler handler;
            try {
                handler = handlers[packetID];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw unknownIDException(this.channel, packetID);
            }

            if (handler == null) {
                throw unknownIDException(this.channel, packetID);
            }

            if ((handler.info & BaseNettyPacket.sideToCode(side)) == 0) {
                throw new ProtocolException("PacketID " + packetID + " received on invalid side " + side);
            }

            if ((handler.info & RawPacket.ASYNC) != 0) {
                handler.accept(this.channel, packetID, in, player);
            } else {
                Scheduler s = player == null || player.worldObj.isRemote ? Scheduler.client() : Scheduler.server();
                SchedulerInternalTask.execute(s, new SchedulerInternalTask() {
                    @Override
                    public boolean run() {
                        handler.accept(SimpleChannelPacketHandler.this.channel, packetID, in, player == null ? Players.getClient() : player);
                        return false;
                    }
                });
            }
        }

        private static ProtocolException unknownIDException(String channel, int packetID) {
            return new ProtocolException(String.format("Unknown packetID %s in channel %s", packetID, channel));
        }

    }

    private static byte getInfo(Class<?> clazz) {
        return BaseNettyPacket.encodeCharacteristics(getReceiver(clazz), isAsync(clazz));
    }

    private static Side getReceiver(Class<?> clazz) {
        Packet.Receiver annotation = clazz.getAnnotation(Packet.Receiver.class);
        return annotation == null ? null : annotation.value();
    }

    private static boolean isAsync(Class<?> clazz) {
        return clazz.isAnnotationPresent(Packet.Async.class);
    }

    private static Handler[] pack(TByteObjectMap<Handler> map) {
        TByteObjectIterator<Handler> it = map.iterator();
        int maxID = 0;
        while (it.hasNext()) {
            it.advance();
            maxID = Math.max(maxID, UnsignedBytes.toInt(it.key()));
        }

        Handler[] arr = new Handler[maxID + 1];
        it = map.iterator();
        while (it.hasNext()) {
            it.advance();
            arr[UnsignedBytes.toInt(it.key())] = it.value();
        }

        return arr;
    }

    private void checkNotBuilt() {
        checkState(handlers != null, "Already built");
    }

    /**
     * <p>Base class for packetID specific handlers. channel and ID are passed here too so they don't need to be captured in
     * every handler object.</p>
     */
    abstract static class Handler {

        final byte info;

        Handler(byte info) {
            this.info = info;
        }

        public abstract void accept(String channel, int packetID, MCDataInput in, EntityPlayer player);

    }

    static <R extends Packet.Response> void sendResponse(R response, EntityPlayer player, int packetID, int responseID, String channel) {
        BaseNettyPacket responseWrap = new ResponseWrapper<>(response, packetID, responseID, channel);

        if (player.worldObj.isRemote) {
            NetworkImpl.sendToServer(responseWrap);
        } else {
            NetworkImpl.sendToPlayer((EntityPlayerMP) player, responseWrap);
        }
    }
}
