package de.take_weiland.mods.commons.internal.sync.builtin;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.sync.SimpleSyncer;

/**
 * @author diesieben07
 */
abstract class SyncerForImmutable<V> implements SimpleSyncer<V, V> {

    private final Class<V> type;

    SyncerForImmutable(Class<V> type) {
        this.type = type;
    }

    @Override
    public final Class<V> getValueType() {
        return type;
    }

    @Override
    public final Class<V> getCompanionType() {
        return type;
    }

    @Override
    public boolean equal(V value, V companion) {
        return Objects.equal(value, companion);
    }
}
