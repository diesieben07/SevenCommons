package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.sync.Syncer;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author diesieben07
 */
abstract class SyncerDefaultEquals<V> implements Syncer.Simple<V, V> {

    private final Class<V> type;

    SyncerDefaultEquals(Class<V> type) {
        this.type = type;
    }

    @Override
    public final Class<V> getCompanionType() {
        return type;
    }

    @Override
    public <T_OBJ> Change<V, V> checkChange(T_OBJ obj, V value, V companion, Consumer<V> companionSetter) {
        if (Objects.equals(value, companion)) {
            return noChange();
        } else {
            return newValue(companion);
        }
    }

}
