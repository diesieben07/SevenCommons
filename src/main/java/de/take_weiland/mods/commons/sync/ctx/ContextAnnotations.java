package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.lang.annotation.Annotation;
import java.util.concurrent.ConcurrentMap;

/**
 * @author diesieben07
 */
public final class ContextAnnotations {

    private static final ConcurrentMap<Class<? extends Annotation>, Entry<?, ?>> map = new MapMaker().concurrencyLevel(2).makeMap();

    public static <T extends Annotation, V> void register(Class<T> annotation, SyncContext.Key<V> key, Function<? super T, ? extends V> function) {
        if (map.putIfAbsent(annotation, new Entry<>(key, function)) != null) {
            throw new IllegalArgumentException("ContextAnnotation " + annotation.getName() + " already registered");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void resolve(Annotation annotation, ImmutableMap.Builder<SyncContext.Key<?>, Object> builder) {
        Entry<?, ?> entry = map.get(annotation.annotationType());
        if (entry != null) {
            // cast is safe, Map is guarded by register()
            Object value = ((Entry) entry).function.apply(annotation);
            if (value != null) {
                builder.put(((Entry) entry).key, value);
            }
        }
    }

    private static final class Entry<T extends Annotation, V> {

        final SyncContext.Key<V> key;
        final Function<? super T, ? extends V> function;

        Entry(SyncContext.Key<V> key, Function<? super T, ? extends V> function) {
            this.key = key;
            this.function = function;
        }
    }

    private ContextAnnotations() { }

}
