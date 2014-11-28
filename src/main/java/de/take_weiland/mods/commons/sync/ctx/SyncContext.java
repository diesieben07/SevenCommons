package de.take_weiland.mods.commons.sync.ctx;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public interface SyncContext<T> {

    TypeToken<T> getGenericType();

    Class<? super T> getRawType();

    <V> V getValue(Key<? extends V> key);

    ImmutableMap<Key<?>, Object> getData();

    public static final class Key<T> {

        private final String name;

        public Key(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
