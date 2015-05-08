package de.take_weiland.mods.commons.sync;

/**
 * @author diesieben07
 */
public interface PropertyAccess<OBJ, T> {

    T get(OBJ object);

    void set(OBJ object, T value);

    Class<T> getType();
}
