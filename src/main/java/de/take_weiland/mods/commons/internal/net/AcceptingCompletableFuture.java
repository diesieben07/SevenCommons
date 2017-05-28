package de.take_weiland.mods.commons.internal.net;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * <p>CompletableFuture which also implements BiConsumer to be completed.</p>
 *
 * @author diesieben07
 */
public class AcceptingCompletableFuture<T> extends CompletableFuture<T> implements BiConsumer<T, Throwable> {
    @Override
    public void accept(T r, Throwable x) {
        if (x == null) {
            complete(r);
        } else {
            completeExceptionally(x);
        }
    }
}
