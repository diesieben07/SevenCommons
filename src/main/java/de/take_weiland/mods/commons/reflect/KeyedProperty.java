package de.take_weiland.mods.commons.reflect;

import java.util.Map;

/**
 * @author diesieben07
 */
public interface KeyedProperty<K, V> extends PropertyAccess<V> {

    V get(Object o, K key);

    void set(Object o, K key, V val);

    @SuppressWarnings("unchecked")
    @Override
    default V get(Object o) {
        return get(((ObjectKeyWrapper) o).o, (K) ((ObjectKeyWrapper) o).key);
    }

    @SuppressWarnings("unchecked")
    @Override
    default void set(Object o, V val) {
        set(((ObjectKeyWrapper) o).o, (K) ((ObjectKeyWrapper) o).key, val);
    }

    final class ObjectKeyWrapper {

        final Object o;
        final Object key;

        ObjectKeyWrapper(Object o, Object key) {
            this.o = o;
            this.key = key;
        }
    }

    static Object wrapKey(Object o, Object key) {
        return new ObjectKeyWrapper(o, key);
    }

    static <K, V> KeyedProperty<K, V> keyed(PropertyAccess<? extends Map<? super K, V>> map) {
        return new KeyedProperty<K, V>() {
            @Override
            public V get(Object o, K key) {
                return map.get(o).get(key);
            }

            @Override
            public void set(Object o, K key, V val) {
                map.get(o).put(key, val);
            }
        };
    }
}
