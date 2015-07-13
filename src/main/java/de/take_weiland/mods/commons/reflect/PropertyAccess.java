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
