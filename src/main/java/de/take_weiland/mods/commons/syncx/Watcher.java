package de.take_weiland.mods.commons.syncx;

/**
 * @author diesieben07
 */
public interface Watcher<T> {

    void setup(SyncableProperty<? extends T> property);

    boolean hasChanged(SyncableProperty<? extends T> property);

    void afterWrite(SyncableProperty<? extends T> property);

}
