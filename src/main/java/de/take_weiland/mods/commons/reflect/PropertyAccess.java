package de.take_weiland.mods.commons.reflect;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public interface PropertyAccess<T> extends Function<Object, T>, BiConsumer<Object, T> {

    T get(Object o);

    void set(Object o, T val);

    @Override
    default void accept(Object o, T t) {
        set(o, t);
    }

    @Override
    default T apply(Object o) {
        return get(o);
    }

    PropertyAccess<Object> EMPTY = new PropertyAccess<Object>() {
        @Override
        public Object get(Object o) {
            return null;
        }

        @Override
        public void set(Object o, Object val) { }
    };
}
