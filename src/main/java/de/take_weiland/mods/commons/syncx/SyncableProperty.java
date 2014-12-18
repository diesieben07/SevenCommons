package de.take_weiland.mods.commons.syncx;

/**
 * @author diesieben07
 */
public interface SyncableProperty<T> {

    T get();

    Object getData();

    void setData(Object data);

}
