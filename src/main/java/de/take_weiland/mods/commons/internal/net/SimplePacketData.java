package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public abstract class SimplePacketData<P extends BaseModPacket> {

    public final String channel;
    public final int packetID;
    final BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor;

    public SimplePacketData(String channel, int packetID, BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor) {
        this.channel = channel;
        this.packetID = packetID;
        this.constructor = constructor;
    }

    public static final class Normal<P extends Packet> extends SimplePacketData<P> {

        public final BiConsumer<? super P, ? super EntityPlayer> handler;

        public Normal(String channel, int packetID, BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
            super(channel, packetID, constructor);
            this.handler = handler;
        }
    }

    public static final class WithResponse<P extends Packet.WithResponse<R>, R extends Packet> extends SimplePacketData<P> {

        final BiFunction<? super P, ? super EntityPlayer, ? extends R> handler;
        final AtomicInteger nextID = new AtomicInteger();
        final ConcurrentMap<Integer, CompletableFuture<R>> futures = new MapMaker().concurrencyLevel(2).makeMap();

        public WithResponse(String channel, int packetID, BiFunction<? super MCDataInput, ? super EntityPlayer, ? extends P> constructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
            super(channel, packetID, constructor);
            this.handler = handler;
        }

        final int nextID() {
            return nextID.getAndIncrement() & 0xFF;
        }
    }

}
