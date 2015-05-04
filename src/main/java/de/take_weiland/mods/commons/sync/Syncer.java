package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import org.omg.IOP.TAG_ORB_TYPE;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    <T_OBJ> Change<T_DATA> checkChange(T_OBJ obj, T_VAL value, T_COM companion, Consumer<T_COM> companionSetter);

    void write(T_DATA value, MCDataOutput out);

    T_DATA read(MCDataInput in);

    <T_OBJ> void applyChange(T_OBJ obj, T_DATA data, T_VAL oldValue, BiConsumer<T_OBJ, T_VAL> setter);

    default Change<T_DATA> noChange() {
        return null;
    }
    default Change<T_DATA> newValue(T_DATA val) {
        return new Change<>(this, val);
    }

    interface Simple<T_VAL, T_COM> extends Syncer<T_VAL, T_VAL, T_COM> {

        @Override
        default <T_OBJ> void applyChange(T_OBJ obj, T_VAL data, T_VAL oldValue, BiConsumer<T_OBJ, T_VAL> setter) {
            setter.accept(obj, data);
        }
    }

    interface ForImmutable<T_VAL> extends Syncer.Simple<T_VAL, T_VAL> {

        @Override
        default <T_OBJ> Change<T_VAL> checkChange(T_OBJ obj, T_VAL value, T_VAL companion, Consumer<T_VAL> companionSetter) {
            if (Objects.equals(value, companion)) {
                return noChange();
            } else {
                companionSetter.accept(value);
                return newValue(value);
            }
        }
    }

    final class Change<T_DATA, T_OBJ> {

        private int id;
        private final Syncer<?, T_DATA, T_OBJ> syncer;
        private final T_DATA val;

        Change(Syncer<?, T_DATA, T_OBJ> syncer, T_DATA val) {
            this.syncer = syncer;
            this.val = val;
        }

        public T_DATA value() {
            return val;
        }

        public void write(MCDataOutput out) {
            out.writeVarInt(id);
            syncer.write(val, out);
        }

        public void apply(T_OBJ obj) {
            syncer.applyChange(obj, );
        }

    }

}
