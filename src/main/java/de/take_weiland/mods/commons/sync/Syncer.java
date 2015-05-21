package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.ChangedValue;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>Support for syncing of a Type {@code V}.</p>
 * <p>An optional companion type may be specified to store additional data to enable syncing.
 * A companion is simply a second field stored together with the actual property. The Syncer can store arbitrary data
 * of type {@code COM} in that field.</p>
 * <p>Changes to the field are represented as type {@code DATA}, which should be immutable or at least semi-immutable,
 * since these change values are passed across the client-server boundary.</p>
 * <p>This methods of this interfaces will be called in the following manner:</p>
 * <ul>
 * <li>{@link #companionType()} - should return a constant value, may be called at any time depending on the
 * underlying implementation.</li>
 * <li>{@link #check(Object, Function, BiConsumer, Function, BiConsumer)} - always called on the server and may be
 * called very often, usually every game tick. If this method returns {@link #newValue(Object)}, the represented data
 * will be passed to the client either directly using {@link #apply(Object, Object, Function, BiConsumer)}
 * or indirectly by encoding and decoding via {@link #encode(Object, MCDataOutput)} and then
 * {@link #apply(MCDataInput, Object, Function, BiConsumer)}.</li>
 * <li>In either case both versions of {@code apply} are always called on the client thread.</li>
 * </ul>
 *
 * @author diesieben07
 */
public interface Syncer<VAL, COM, DATA> {

    /**
     * <p>The companion type for this Syncer. May be {@code null} if no companion is needed.</p>
     * <p>The initial value for the companion will be {@code null} for reference types and {@code 0} for primitives.</p>
     *
     * @return the companion type
     */
    Class<COM> companionType();

    default Change<DATA> check(Object obj, PropertyAccess<VAL> property, PropertyAccess<COM> companion) {
        return check(obj, property, property, companion, companion);
    }

    /**
     * <p>Check if the property represented by the given getters and setters has changed.</p>
     * <p>This method must return one of {@link #noChange()} or {@link #newValue(Object)}.</p>
     * <p>If this Syncer has no companion ({@link #companionType()} is null), {@code cGetter} and {@code cSetter}
     * will be {@code null}.</p>
     * <p>This method is always called on the server.</p>
     *
     * @param obj     the object
     * @param getter  the getter for the property
     * @param setter  the setter for the property
     * @param cGetter the getter for the companion
     * @param cSetter the setter for the companion
     * @return {@link #noChange()} or {@link #newValue(Object)}
     */
    <OBJ> Change<DATA> check(OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter,
                             Function<OBJ, COM> cGetter, BiConsumer<OBJ, COM> cSetter);


    /**
     * <p>Encode the change value into the output stream.</p>
     * <p>This method is always called on the server.</p>
     *
     * @param data the data value
     * @param out  the output stream
     */
    void encode(DATA data, MCDataOutput out);

    default void apply(DATA data, Object obj, PropertyAccess<VAL> property, PropertyAccess<COM> companion) {
        apply(data, obj, property, property);
    }

    /**
     * <p>Called when the client receives a direct update for the field represented by the given getter and setter.</p>
     * <p>This method must perform any actions necessary to apply the given data to the field.</p>
     * <p>This method is always called on the client.</p>
     *
     * @param data   the data value as returned by {@link #check(Object, Function, BiConsumer, Function, BiConsumer)}
     * @param obj    the object
     * @param getter the getter for the property
     * @param setter the setter for the property
     */
    <OBJ> void apply(DATA data, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter);

    default void apply(MCDataInput in, Object obj, PropertyAccess<VAL> property, PropertyAccess<COM> companion) {
        apply(in, obj, property, property);
    }

    /**
     * <p>Called when the client receives an update for the field represented by the given getter and setter.</p>
     * <p>This method must perform any actions necessary to apply the given data to the field.</p>
     * <p>This method is always called on the client.</p>
     *
     * @param in     the input stream containing the data as written by {@link #encode(Object, MCDataOutput)}
     * @param obj    the object
     * @param getter the getter for the property
     * @param setter the setter for the property
     */
    <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter);

    /**
     * <p>To be returned by {@link #check(Object, Function, BiConsumer, Function, BiConsumer)} when the property has not changed.</p>
     * <p>This method usually not be overwritten.</p>
     *
     * @return always null
     */
    default Change<DATA> noChange() {
        return null;
    }

    /**
     * <p>To be returned by {@link #check(Object, Function, BiConsumer, Function, BiConsumer)} when the property has changed.</p>
     * <p>The data value will then be passed to {@link #apply(Object, Object, Function, BiConsumer)} or be sent to the client via a packet.</p>
     * <p>This method should not be overwritten.</p>
     *
     * @param data the data value representing the change
     * @return an object representing the changed value
     */
    default Change<DATA> newValue(DATA data) {
        return new Change<>(this, data);
    }

    /**
     * <p>Representation of a changed value. Obtain via {@link #noChange()} or {@link #newValue(Object)}.</p>
     */
    final class Change<T_DATA> extends ChangedValue<T_DATA> {

        // no instances for anyone else but me :D
        Change(Syncer<?, ?, T_DATA> syncer, T_DATA data) {
            super(syncer, data);
        }
    }

    /**
     * <p>Small helper interface for implementing a Syncer.</p>
     */
    interface Simple<VAL, COM, DATA> extends Syncer<VAL, COM, DATA> {

        /**
         * <p>Same functionality as {@link Syncer#check(Object, Function, BiConsumer, Function, BiConsumer)}, but with
         * getter and setter already applied.</p>
         *
         * @param value     the value of the property
         * @param companion the value of the companion
         * @param obj       the object
         * @param setter    the setter for the property
         * @param cSetter   the setter for the companion
         * @return {@link #noChange()} or {@link #newValue(Object)}
         */
        <OBJ> Change<DATA> check(VAL value, COM companion, OBJ obj, BiConsumer<OBJ, VAL> setter, BiConsumer<OBJ, COM> cSetter);

        @Override
        default <OBJ> Change<DATA> check(OBJ obj, Function<OBJ, VAL> getter, BiConsumer<OBJ, VAL> setter, Function<OBJ, COM> cGetter, BiConsumer<OBJ, COM> cSetter) {
            return check(getter.apply(obj), cGetter.apply(obj), obj, setter, cSetter);
        }
    }

    /**
     * <p>Skeletal implementation for Syncers of immutable types (like {@link java.util.UUID}.</p>
     * <p>This implementation uses the same type for companion, data and value and uses {@link Objects#equals(Object, Object)}
     * to check for changes.</p>
     */
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

        /**
         * <p>Decode a value from the input stream. The stream contains the data as it was written by {@link #encode(Object, MCDataOutput)}.</p>
         *
         * @param in the input stream
         * @return the data
         */
        VAL decode(MCDataInput in);
    }

}
