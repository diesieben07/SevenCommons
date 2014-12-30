package de.take_weiland.mods.commons.sync;

/**
 * <p>Representation of a property marked with {@code &#0064;Sync}.</p>
 *
 * @author diesieben07
 */
public interface SyncableProperty<T> {

    /**
     * <p>Get the value of the property.</p>
     * @return the value
     */
    T get();

    /**
     * <p>Set the value of the property, if it is writable.</p>
     * @param value the new value
     */
    void set(T value);

    /**
     * <p>Get the data object associated with the property, initially {@code null}.</p>
     * @return the data object
     */
    Object getData();

    /**
     * <p>Set the data object associated with the property.</p>
     * @param data the data object
     */
    void setData(Object data);

}
