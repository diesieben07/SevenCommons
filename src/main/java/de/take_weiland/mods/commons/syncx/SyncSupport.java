package de.take_weiland.mods.commons.syncx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
public final class SyncSupport {

    private static Map<Class<?>, Object> registry = new HashMap<>();

    public static <T> Builder<T> sync(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    public static <T, X> void register(Class<T> clazz, Watcher<? super T> watcher) {
        checkNotFrozen();
        checkArgument(!registry.containsKey(clazz), "Watcher registration for " + clazz + " already present");

        registry.put(clazz, watcher);
    }

    public static void register(Class<?> clazz, WatcherProvider provider) {
        checkNotFrozen();
        Object obj = registry.get(clazz);
        checkArgument(!(obj instanceof Watcher), "Single Watcher for " + clazz + " already present");

        if (obj == null) {
            registry.put(clazz, (obj = new ArrayList<WatcherProvider>()));
        }
        //noinspection unchecked
        ((List<WatcherProvider>) obj).add(provider);
    }

    private static void checkNotFrozen() {
        checkState(!(registry instanceof ImmutableMap), "Register watchers before postInit");
    }

    public static <T> Watcher<? super T> getWatcher(TypeToken<T> type) {
        for (Class<?> rawType : JavaUtils.hierarchy(type.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
            Watcher<? super T> watcher = null;
            Object obj = registry.get(rawType);
            if (obj instanceof Watcher) {
                //noinspection unchecked
                watcher = (Watcher<? super T>) obj;
            } else if (obj != null) {
                //noinspection unchecked
                watcher = getWatcher(type, (List<WatcherProvider>) obj);
            }
            if (watcher != null) {
                return watcher;
            }
        }
        throw new IllegalArgumentException("No Watcher found for " + type);
    }

    private static <T> Watcher<T> getWatcher(TypeToken<T> type, List<WatcherProvider> providers) {
        Watcher<T> watcher = null;
        for (WatcherProvider provider : providers) {
            if (watcher == null) {
                watcher = provider.getWatcher(type);
            } else if (provider.getWatcher(type) != null) {
                throw new IllegalStateException("Multiple watchers found for " + type);
            }
        }
        return watcher;
    }

    static void freeze() {
        ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
        for (Map.Entry<Class<?>, Object> entry : registry.entrySet()) {
            if (entry.getValue() instanceof List) {
                builder.put(entry.getKey(), ImmutableList.copyOf((List<?>) entry.getValue()));
            } else {
                builder.put(entry.getKey(), entry.getValue());
            }
        }
        registry = builder.build();
    }

    public static final class Builder<T> {

        private final Class<T> clazz;
        private

        Builder(Class<T> clazz) {
            this.clazz = clazz;
        }

        public void with(Watcher<? super T> watcher) {
            register(clazz, watcher);
        }

    }

    private SyncSupport() {}
}
