package de.take_weiland.mods.commons.reflect;

import de.take_weiland.mods.commons.internal.prop.ListIteratingProperty;
import de.take_weiland.mods.commons.internal.prop.MapIteratingProperty;

import java.util.List;
import java.util.Map;

/**
 * <p>Allows iteration over a property with an iterable value such as a collection.</p>
 * <p>Usage:</p>
 * <ul>
 *     <li>Call {@link #start(Object)} to begin the iteration.</li>
 *     <li>Call {@link #moveNext()} until it returns false to retrieve all elements in order.</li>
 *     <li>{@link #get(Object)}, {@link #set(Object, Object)} and similar methods are only valid to be called in between calls to
 *     {@link #moveNext()} which returned {@code true}.</li>
 *     <li>Call {@link #done()} to prematurely end the iteration.</li>
 *     <li>Due to the design of this interface it is inherently not threadsafe.</li>
 * </ul>
 *
 * @author diesieben07
 */
public interface IteratingProperty<T> extends PropertyAccess<T> {

    /**
     * <p>Starting the iteration process using the given object.</p>
     * @param o the object
     */
    void start(Object o);

    /**
     * <p>Move to the next element if one exists. Returns true if there was a next element.</p>
     * @return true if there was a next element
     */
    boolean moveNext();

    /**
     * <p>Clean up the internal state of the iteration after the iteration is done. This method should be called when
     * the iteration is stopped before all elements have been received. The iteration is also ended automatically as soon
     * as {@link #moveNext()} returns false.</p>
     */
    void done();

    /**
     * <p>Create a property that will iterate the values </p>
     * @param mapProperty
     * @param <K>
     * @param <V>
     * @return
     */
    static <K, V> IteratingProperty<V> iterateMap(PropertyAccess<? extends Map<? super K, V>> mapProperty) {
        return new MapIteratingProperty<>(mapProperty);
    }

    static <T> IteratingProperty<T> iterateList(PropertyAccess<? extends List<T>> listProperty) {
        return new ListIteratingProperty<>(listProperty);
    }
}