package de.take_weiland.mods.commons.sync;


/**
 * <p>Representation of a property with a companion object.</p>
 *
 * @author diesieben07
 */
public interface SyncableProperty<T, OBJ> extends Property<T,OBJ> {

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
