package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.ChangedValue;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Support for syncing of a Type {@code V}.</p>
 * <p>An optional companion type may be specified to store additional data to enable syncing.</p>
 *
 * @author diesieben07
 */
public interface Syncer<VAL, COM, DATA> {

    Class<COM> companionType();

    <OBJ> Change<DATA> check(OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter,
                             Function<OBJ, COM> cGetter, BiConsumer<OBJ, COM> cSetter);

    void encode(DATA data, MCDataOutput out);

    <OBJ> void apply(DATA data, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter);

    <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter);

    default Change<DATA> noChange() {
        return null;
    }

    default Change<DATA> newValue(DATA data) {
        return new ChangedValue<>(this, data);
    }

    interface Change<T_DATA> { }

    interface Simple<VAL, COM, DATA> extends Syncer<VAL, COM, DATA> {

        <OBJ> Change<DATA> check(VAL value, COM companion, OBJ obj, BiConsumer<OBJ, VAL> setter, BiConsumer<OBJ, COM> cSetter);

        @Override
        default <OBJ> Change<DATA> check(OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter, Function<OBJ, COM> cGetter, BiConsumer<OBJ, COM> cSetter) {
            return check(getter.apply(obj), cGetter.apply(obj), obj, setter, cSetter);
        }
    }

    interface ForImmutable<VAL> extends Syncer.Simple<VAL, VAL, VAL> {

        @Override
        default <OBJ> Change<VAL> check(VAL value, VAL companion, OBJ obj, BiConsumer<OBJ, VAL> setter, BiConsumer<OBJ, VAL> cSetter) {
            if (Objects.equals(value, companion)) {
                return noChange();
            } else {
                cSetter.accept(obj, value);
                return newValue(value);
            }
        }

        @Override
        default <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter) {
            setter.accept(obj, decode(in));
        }

        @Override
        default <OBJ> void apply(VAL val, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter) {
            setter.accept(obj, val);
        }

        VAL decode(MCDataInput in);
    }

}
