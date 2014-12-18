package de.take_weiland.mods.commons.syncx.impl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;

import java.util.BitSet;

/**
 * @author diesieben07
 */
public enum BitSetWatcher implements Watcher<BitSet> {
    INSTANCE;

    @Override
    public boolean hasChanged(SyncableProperty<? extends BitSet> property) {
        return !Objects.equal(property.get(), property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends BitSet> property) {
        BitSet val = property.get();
        if (val == null) {
            property.setData(null);
        } else {
            BitSet comp = (BitSet) property.getData();
            if (comp == null) {
                property.setData((BitSet) val.clone());
            } else {
                comp.clear();
                comp.or(val);
            }
        }
    }


    @Override
    public void setup(SyncableProperty<? extends BitSet> property) { }
}
