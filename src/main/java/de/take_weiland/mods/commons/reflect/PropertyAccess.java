package de.take_weiland.mods.commons.reflect;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public interface PropertyAccess<T> extends Function<Object, T>, BiConsumer<Object, T> {

    T get(Object o);

    void set(Object o, T val);

    @SuppressWarnings("unchecked")
    default void setBoolean(Object o, boolean val) {
        set(o, (T) (Boolean) val);
    }

    @SuppressWarnings("unchecked")
    default void setByte(Object o, byte val) {
        set(o, (T) (Byte) val);
    }

    @SuppressWarnings("unchecked")
    default void setShort(Object o, short val) {
        set(o, (T) (Short) val);
    }

    @SuppressWarnings("unchecked")
    default void setChar(Object o, char val) {
        set(o, (T) (Character) val);
    }

    @SuppressWarnings("unchecked")
    default void setInt(Object o, int val) {
        set(o, (T) (Integer) val);
    }

    @SuppressWarnings("unchecked")
    default void setLong(Object o, long val) {
        set(o, (T) (Long) val);
    }

    @SuppressWarnings("unchecked")
    default void setFloat(Object o, float val) {
        set(o, (T) (Float) val);
    }

    @SuppressWarnings("unchecked")
    default void setDouble(Object o, double val) {
        set(o, (T) (Double) val);
    }

    default boolean getBoolean(Object o) {
        return (Boolean) get(o);
    }

    default byte getByte(Object o) {
        return (Byte) get(o);
    }

    default short getShort(Object o) {
        return (Short) get(o);
    }

    default char getChar(Object o) {
        return (Character) get(o);
    }

    default int getInt(Object o) {
        return (Integer) get(o);
    }

    default long getLong(Object o) {
        return (Long) get(o);
    }

    default float getFloat(Object o) {
        return (Float) get(o);
    }

    default double getDouble(Object o) {
        return (Double) get(o);
    }

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
        public void set(Object o, Object val) {
        }
    };
}
