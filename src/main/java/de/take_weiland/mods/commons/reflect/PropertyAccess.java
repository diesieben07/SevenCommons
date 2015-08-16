package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.internal.prop.ListAccessProperty;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Possibly optimized access to a {@link Property}.</p>
 * <p>Every property is also a {@link Function} (delegates to the getter functionality) and a
 * {@link BiConsumer} (delegates to the setter functionality).</p>
 *
 * @author diesieben07
 */
public interface PropertyAccess<T> extends Function<Object, T>, BiConsumer<Object, T> {

    /**
     * <p>Get the property from the given Object.</p>
     *
     * @param o the object
     * @return the value of the property
     */
    T get(Object o);

    /**
     * <p>Set the property in the given Object.</p>
     * @param o the object
     * @param val the new value for the property
     */
    void set(Object o, T val);

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

    @SuppressWarnings("unchecked")
    default void setBoolean(Object o, boolean v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setByte(Object o, byte v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setShort(Object o, short v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setChar(Object o, char v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setInt(Object o, int v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setLong(Object o, long v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setFloat(Object o, float v) {
        set(o, (T) (Object) v);
    }

    @SuppressWarnings("unchecked")
    default void setDouble(Object o, double v) {
        set(o, (T) (Object) v);
    }

    @Override
    default void accept(Object o, T t) {
        set(o, t);
    }

    @Override
    default T apply(Object o) {
        return get(o);
    }

    static <T> PropertyAccess<T> makeListAccess(PropertyAccess<? extends List<T>> listProperty) {
        return new ListAccessProperty<>(listProperty);
    }

    static Object listAccessObject(Object obj, int index) {
        return new ListAccessProperty.ObjectIntBox(obj, index);
    }

}
