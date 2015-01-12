package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.AnnotationNull;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.SerializationMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A Watcher handles detecting changes in a {@link de.take_weiland.mods.commons.sync.SyncableProperty} and
 * encoding it's value into an {@linkplain de.take_weiland.mods.commons.net.MCDataOutput OutputStream}.</p>
 *
 * @author diesieben07
 */
public interface Watcher<T> {

    /**
     * <p>Initial setup for the given property. Called once per property and before any other methods.
     * This method is usually used to set-up any necessary data-objects for the property.</p>
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void setup(SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Write the value to the stream no matter if it has been changed or not.
     * This is used to send the "initial" value of a property to the client when the client starts tracking the property
     * (e.g. by entering a chunk).</p>
     * @param out the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Determine if the given property has changed since the last call to {@code writeAndUpdate}.</p>
     * <p>This method is only called on the server.</p>
     * @param property the property
     * @param instance the instance
     * @return true if the property has changed
     */
    <OBJ> boolean hasChanged(SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Write the value of the property to the stream and mark it's current state as "up to date".</p>
     * <p>This method is only called on the server.</p>
     * @param out the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>Update the value of the property by reading the value from the stream.</p>
     * <p>This method is only called on the client.</p>
     * @param in the stream
     * @param property the property
     * @param instance the instance
     */
    <OBJ> void read(MCDataInput in, SyncableProperty<T, OBJ> property, OBJ instance);

    /**
     * <p>A Provider for Watchers.</p>
     *
     * <p>When applied to a method, this method must be static and accept a single parameter of type
     * {@link de.take_weiland.mods.commons.serialize.PropertyMetadata}. It's return type must be any non-primitive type.</p>
     *
     * <p>When applied to a field, this field must be static and final. It's type must be assignable to {@code Watcher}.
     * The {@linkplain #method() SerializationMethod filter} must be set when applied to fields.</p>
     *
     * <p>The TypeSpecification dictates what this provider must provide. The TypeSpecification will only ever report
     * the concrete SerializationMethods {@code VALUE} or {@code CONTENTS}. The provider must then return an instance of
     * {@code Watcher} that implements the given semantics. If this provider cannot provide a
     * Watcher for the given TypeSpecification, {@code null} must be returned.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface Provider {

        /**
         * <p>The base type that this provider can provide Watchers for. This provider will only be queried for that
         * type or subtypes. In other words: if this attribute reports {@code T.class}, this provider will only be queried
         * for TypeSpecifications of type {@code TypeSpecification&lt;? extends T&gt;}.</p>
         *
         * <p>If this method is left at default, the class declaring the provider will be used.</p>
         *
         * @return the base type
         */
        // if name is changed, need to update SerializerRegistry as well!
        Class<?> forType() default AnnotationNull.class;

        /**
         * <p>A filter for the SerializationMethod that this provider supports. If {@code VALUE} or {@code CONTENTS}
         * are specified, this provider will only be queried for types that demand that SerializationMethod.</p>
         *
         * <p>A filter is mandatory for fields.</p>
         *
         * @return a filter
         */
        SerializationMethod method() default SerializationMethod.DEFAULT;

    }
}
