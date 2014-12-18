package de.take_weiland.mods.commons.syncx.impl;

import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;

/**
 * @author diesieben07
 */
public enum IdentityWatcher implements Watcher<Object> {
    INSTANCE;

    @Override
    public void setup(SyncableProperty<?> property) {

    }

    @Override
    public boolean hasChanged(SyncableProperty<?> property) {
        return property.get() != property.getData();
    }

    @Override
    public void afterWrite(SyncableProperty<?> property) {
        property.setData(property.get());
    }
}
