package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
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
        boolean async = isAsync(packetClass);

        PacketToChannelMap.register(packetClass, channel, id, async, handler);
        addHandler(id, new NormalHandler<>(async, handler, constructor));
        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        validateID(id);

        Class<P> packetClass = constructor.getPacketClass();
        boolean async = isAsync(packetClass);

        PacketToChannelMap.register(packetClass, channel, id, async, handler);

        addHandler(id, new ResponseNormalHandler<>(async, constructor, responseConstructor, handler));

        return this;
    }

    @Override
    public <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerFutureResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler) {
        validateID(id);

        Class<P> packetClass = constructor.getPacketClass();
        boolean async = isAsync(packetClass);

        PacketToChannelMap.registerFuture(packetClass, channel, id, async, handler);

        addHandler(id, new ResponseFutureHandler<>(async, constructor, responseConstructor, handler));

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

        Network.registerHandler(this.channel, new SimpleChannelPacketHandler(handlers, channel), true);
    }

    private static class SimpleChannelPacketHandler implements PacketHandler {
        private final Handler[] handlers;
        private final String channel;

        public SimpleChannelPacketHandler(Handler[] handlers, String channel) {
            this.handlers = handlers;
            this.channel = channel;
        }

        @Override
        public void accept(byte[] payload, EntityPlayer player, Side side) {
            MCDataInput in = Network.newInput(payload);
            int packetID = in.readUnsignedByte();
            Handler handler;
            try {
                handler = handlers[packetID];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw unknownIDException(channel, packetID);
            }

            if (handler == null) {
                throw unknownIDException(channel, packetID);
            }

            if (handler.async) {
                handler.accept(channel, packetID, in, player);
            } else {
                Scheduler s = player == null || player.worldObj.isRemote ? Scheduler.client() : Scheduler.server();
                SchedulerInternalTask.execute(s, new SchedulerInternalTask() {
                    @Override
                    public boolean run() {
                        handler.accept(channel, packetID, in, player == null ? Players.getClient() : player);
                        return false;
                    }
                });
            }
        }

        private static ProtocolException unknownIDException(String channel, int packetID) {
            return new ProtocolException(String.format("Unknown packetID %s in channel %s", packetID, channel));
        }

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

        final boolean async;

        Handler(boolean async) {
            this.async = async;
        }

        public abstract void accept(String channel, int packetID, MCDataInput in, EntityPlayer player);

    }

    static <R extends Packet.Response> void sendResponse(R response, EntityPlayer player, int packetID, int responseID, String channel) {
        BaseNettyPacket responseWrap = new ResponseWrapper<>(response, packetID, responseID, channel);

        if (player.worldObj.isRemote) {
            Network.sendToServer(responseWrap);
        } else {
            Network.sendToPlayer((EntityPlayerMP) player, responseWrap);
        }
    }
}
