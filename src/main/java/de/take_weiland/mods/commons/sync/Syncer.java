package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Support for syncing of a Type {@code V}.</p>
 * <p>An optional companion type may be specified to store additional data to enable syncing.</p>
 *
 * @author diesieben07
 */
public interface Syncer<T_VAL, T_DATA, T_COM> {

    /**
     * <p>The companion type for this Syncer.</p>
     * <p>The initial value for the companion will be {@code null} for reference types and {@code 0} for primitives.</p>
     */
    Class<T_COM> getCompanionType();

    <T_OBJ, T_COMP_H> ChangeEvent<T_DATA> hasChanged(T_OBJ obj, Function<T_OBJ, T_VAL> getter, BiConsumer<T_OBJ, T_VAL> setter,
                                                     T_COMP_H companionHolder, Function<T_COMP_H, T_COM> companionGetter, BiConsumer<T_COMP_H, T_VAL> companionSetter);

    void write(T_DATA data, MCDataOutput out);

    T_DATA read(MCDataInput in);

    <T_OBJ, T_COMP_H> void apply(T_DATA data,
                                 T_OBJ obj, Function<T_OBJ, T_VAL> getter, BiConsumer<T_OBJ, T_VAL> setter,
                                 T_COMP_H companionHolder, Function<T_COMP_H, T_COM> companionGetter, BiConsumer<T_COMP_H, T_VAL> companionSetter);

    default ChangeEvent<T_DATA> noChange() {
        return null;
    }

    default ChangeEvent<T_DATA> newValue(T_DATA data) {
        return new ChangeEvent<>(this, data);
    }

    final class ChangeEvent<T_DATA> {

        final Syncer<?, T_DATA, ?> syncer;
        final T_DATA value;

        public ChangeEvent(Syncer<?, T_DATA, ?> syncer, T_DATA value) {
            this.syncer = syncer;
            this.value = value;
        }
    }

}
