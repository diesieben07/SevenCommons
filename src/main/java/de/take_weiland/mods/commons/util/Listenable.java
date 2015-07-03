package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.ListenableInternal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Simple event dispatcher.</p>
 * <p>Usage: Implement this interface on your object and it will gain a list of Listeners to which events of type
 * {@code T} can be dispatched.</p>
 * <p>This implementation is not thread-safe.</p>
 *
 * @author diesieben07
 */
public interface Listenable<T> {

    /**
     * <p>Add a listener.</p>
     *
     * @param listener the listener
     */
    default void addListener(Consumer<? super T> listener) {
        ListenableInternal.add((ListenableInternal<T>) this, checkNotNull(listener));
    }

    /**
     * <p>Remove a listener.</p>
     *
     * @param listener the listener
     */
    default void removeListener(Consumer<? super T> listener) {
        ListenableInternal.remove((ListenableInternal<T>) this, listener);
    }

    /**
     * <p>Dispatch an event to all registered listeners.</p>
     *
     * @param event the event
     */
    default void dispatch(T event) {
        ListenableInternal.doDispatch((ListenableInternal<T>) this, event);
    }

    /**
     * <p>Dispatch an event to all registered listeners. The supplier is only queried once when there are
     * actually any registered listeners.</p>
     * <p>Use this method instead of {@link #dispatch(Object)} if the event creation is somewhat expensive
     * and outweighs the overhead of the supplier.</p>
     *
     * @param eventSupplier the supplier
     */
    default void dispatch(Supplier<? extends T> eventSupplier) {
        ListenableInternal.doDispatch((ListenableInternal<T>) this, eventSupplier);
    }

}
