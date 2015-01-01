package de.take_weiland.mods.commons.sync;

/**
 * <p>Representation of a property marked with {@code &#0064;Sync}.</p>
 *
 * @author diesieben07
 */
public interface SyncableProperty<T, OBJ> extends PropertyMetadata<T> {

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

    /**
     * <p>Get the data object associated with the property, initially {@code null}.</p>
     * @param instance the instance
     * @return the data object
     */
    Object getData(OBJ instance);

    /**
     * <p>Set the data object associated with the property.</p>
     * @param data the data object
     * @param instance the instance
     */
    void setData(Object data, OBJ instance);

}
