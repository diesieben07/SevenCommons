package de.take_weiland.mods.commons.syncx.impl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.syncx.SyncSupport;
import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.EnumSet;

/**
 * @author diesieben07
 */
public enum EnumSetWatcher implements Watcher<EnumSet<?>> {
    INSTANCE;

    public static void register() {
        // we can handle any EnumSet
        //noinspection unchecked,rawtypes
        SyncSupport.register(EnumSet.class, (Watcher) INSTANCE);
    }

    @Override
    public void setup(SyncableProperty<? extends EnumSet<?>> property) { }

    @Override
    public boolean hasChanged(SyncableProperty<? extends EnumSet<?>> property) {
        return !Objects.equal(property.get(), property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends EnumSet<?>> property) {
        EnumSet<?> val = property.get();
        if (val == null) {
            property.setData(null);
        } else {
            EnumSet<?> data = (EnumSet<?>) property.getData();
            if (data == null || JavaUtils.getType(val) != JavaUtils.getType(data)) {
                property.setData(val.clone());
            } else {
                data.clear();
                // we check that this works
                //noinspection unchecked,rawtypes
                data.addAll((EnumSet) val);
            }
        }
    }
}
