package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

/**
 * <p>Representation of a property of type {@code T} in class {@code T}.</p>
 *
 * @author diesieben07
 */
public interface Property<T, OBJ> extends TypeSpecification<T> {
    /**
     * <p>Get the value of the property.</p>
     * @param instance the instance
     * @return the value
     */
    T get(OBJ instance);

    /**
     * <p>Set the value of the property, if it is writable.</p>
     * @param value the new value
     * @param instance the instance
     */
    void set(T value, OBJ instance);
}
