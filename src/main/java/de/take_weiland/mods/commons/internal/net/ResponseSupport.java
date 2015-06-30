package de.take_weiland.mods.commons.internal.net;

import com.google.common.collect.MapMaker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author diesieben07
 */
public final class ResponseSupport {

    private static final AtomicInteger nextID = new AtomicInteger();
    private static final int RESPONSE = 0x8000_0000;
    private static final int MASK = ~RESPONSE;

    private static final ConcurrentMap<Integer, CompletableFuture<?>> map = new MapMaker().concurrencyLevel(2).makeMap();

    public static int nextID() {
        return nextID.getAndIncrement() & MASK;
    }

    public static boolean isResponse(int id) {
        return (id & RESPONSE) != 0;
    }

    public static int toResponse(int id) {
        return id | RESPONSE;
    }

    public static void register(int id, CompletableFuture<?> future) {
        map.put(id, future);
    }

    public static CompletableFuture<?> get(int id) {
        return map.remove(id & MASK);
    }

}
