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
 * will be passed to the client either directly using {@link #apply(Object, Object, PropertyAccess, Object, PropertyAccess)}
 * or indirectly by encoding and decoding via {@link #encode(Object, MCDataOutput)} and then
 * {@link #apply(MCDataInput, Object, PropertyAccess, Object, PropertyAccess)}.</li>
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

    /**
     * <p>Check if the property represented by the given getters and setters has changed.</p>
     * <p>This method must return one of {@link #noChange()} or {@link #newValue(Object)}.</p>
     * <p>If this Syncer has no companion ({@link #companionType()} is null), {@code cGetter} and {@code cSetter}
     * will be {@code null}.</p>
     * <p>This method is always called on the server.</p>
     *
     * @param obj       the object to access the property
     * @param property  the property
     * @param cObj      the object to access the companion
     * @param companion the companion property
     * @return {@link #noChange()} or {@link #newValue(Object)}
     */
    Change<DATA> check(Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<COM> companion);


    /**
     * <p>Encode the change value into the output stream.</p>
     * <p>This method is always called on the server.</p>
     *
     * @param data the data value
     * @param out  the output stream
     */
    void encode(DATA data, MCDataOutput out);

    /**
     * <p>Called when the client receives a direct update for the field represented by the given getter and setter.</p>
     * <p>This method must perform any actions necessary to apply the given data to the field.</p>
     * <p>This method is always called on the client.</p>
     *
     * @param data      the data value as returned by {@link #check(Object, PropertyAccess, Object, PropertyAccess)}
     * @param obj       the object to access the property
     * @param property  the property
     * @param cObj      the object to access the companion
     * @param companion the companion property
     */
    void apply(DATA data, Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<COM> companion);


    /**
     * <p>Called when the client receives an update for the field represented by the given getter and setter.</p>
     * <p>This method must perform any actions necessary to apply the given data to the field.</p>
     * <p>This method is always called on the client.</p>
     *
     * @param in        the input stream containing the data as written by {@link #encode(Object, MCDataOutput)}
     * @param obj       the object to access the property
     * @param property  the property
     * @param cObj      the object to access the companion
     * @param companion the companion property
     */
    void apply(MCDataInput in, Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<COM> companion);

    /**
     * <p>To be returned by {@link #check(Object, PropertyAccess, Object, PropertyAccess)} when the property has not changed.</p>
     * <p>This method should not be overwritten.</p>
     *
     * @return always null
     */
    default Change<DATA> noChange() {
        return null;
    }

    /**
     * <p>To be returned by {@link #check(Object, PropertyAccess, Object, PropertyAccess)} when the property has changed.</p>
     * <p>The data value will then be passed to {@link #apply(Object, Object, PropertyAccess, Object, PropertyAccess)} or be sent to the client via a packet.</p>
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
     * <p>Skeletal implementation for Syncers of immutable types (like {@link java.util.UUID}.</p>
     * <p>This implementation uses the same type for companion, data and value and uses {@link Objects#equals(Object, Object)}
     * to check for changes.</p>
     */
    interface ForImmutable<VAL> extends Syncer<VAL, VAL, VAL> {

        @Override
        default Change<VAL> check(Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<VAL> companion) {
            VAL value = property.get(obj);
            if (Objects.equals(value, companion.get(cObj))) {
                return noChange();
            } else {
                companion.set(cObj, value);
                return newValue(value);
            }
        }

        @Override
        default void apply(VAL val, Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<VAL> companion) {
            property.set(obj, val);
        }

        @Override
        default void apply(MCDataInput in, Object obj, PropertyAccess<VAL> property, Object cObj, PropertyAccess<VAL> companion) {
            property.set(obj, decode(in));
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
