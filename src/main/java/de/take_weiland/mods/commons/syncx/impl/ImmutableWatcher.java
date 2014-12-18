package de.take_weiland.mods.commons.syncx.impl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;

/**
 * @author diesieben07
 */
public enum ImmutableWatcher implements Watcher<Object> {
    INSTANCE;

    @Override
    public void setup(SyncableProperty<? extends Object> property) { }

    @Override
    public boolean hasChanged(SyncableProperty<? extends Object> property) {
        return !Objects.equal(property.get(), property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends Object> property) {
        property.setData(property.get());
    }
}
