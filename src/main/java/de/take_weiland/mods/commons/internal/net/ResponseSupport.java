package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.net.Packet;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author diesieben07
 */
public final class ResponseSupport {

    private static final AtomicInteger nextID = new AtomicInteger();
    public static final byte IS_RESPONSE = (byte) 0b10000000;
    private static final byte MASK = ~IS_RESPONSE;

    private static final ConcurrentMap<Integer, CompletableFuture<?>> map = new MapMaker().concurrencyLevel(2).makeMap();

    public static byte register(CompletableFuture<?> future) {
        int id = nextID.getAndIncrement() & MASK;
        if (map.putIfAbsent(id, future) != null) {
            throw new RuntimeException("Ran out of pending response IDs.");
        }
        return (byte) id;
    }

    public static <R extends Packet.Response> CompletableFuture<R> unregister(int id) {
        //noinspection unchecked
        CompletableFuture<R> future = (CompletableFuture<R>) map.remove(id & MASK);
        if (future == null) {
            NetworkImpl.LOGGER.error("Tried to complete unknown packet response future (id={})", id & MASK);
            // just make it go nowhere
            future = new CompletableFuture<>();
        }
        return future;
    }

}
