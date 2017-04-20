package de.take_weiland.mods.commons.serialize.bytes;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.BaseSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.SerializerFactory;
import de.take_weiland.mods.commons.serialize.SerializerRegistry;

import javax.annotation.Nullable;

/**
 * <p>A serializer that can serialize values of type {@code T} to and from a byte stream.</p>
 *
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> extends BaseSerializer<T> {

    default ByteStreamSerializer.Value<T> asValueSerializer() {
        return new ByteValueSerializerShim<>(this);
    }

    default ByteStreamSerializer.Contents<T> asContentsSerializer() {
        return new ByteContentsSerializerShim<>(this);
    }

    void write(MCDataOutput out, PropertyAccess<T> property, Object obj);

    void read(MCDataInput in, PropertyAccess<T> property, Object obj);

    @Override
    Characteristics characteristics();

    /**
     * <p>A ByteStreamSerializer for types where de-serialization produces a new instance.
     * An example is String.</p>
     *
     */
    interface Value<T> extends ByteStreamSerializer<T> {

        @Override
        default Value<T> asValueSerializer() {
            return this;
        }

        @Override
        default Contents<T> asContentsSerializer() {
            throw new UnsupportedOperationException();
        }

        /**
         * <p>Read an instance of type T from the InputStream.</p>
         *
         * @param in the InputStream
         * @return the instance
         */
        T read(MCDataInput in);

        /**
         * <p>Serialize the given instance to the OutputStream.</p>
         *
         * @param out      the OutputStream
         * @param instance the instance
         */
        void write(MCDataOutput out, T instance);

        @Override
        default void write(MCDataOutput out, PropertyAccess<T> property, Object obj) {
            write(out, property.get(obj));
        }

        @Override
        default void read(MCDataInput in, PropertyAccess<T> property, Object obj) {
            property.set(obj, read(in));
        }
    }

    /**
     * <p>A ByteStreamSerializer for types where de-serialization changes the contents of an existing instance.
     * An example is FluidTank.</p>
     */
    interface Contents<T> extends ByteStreamSerializer<T> {

        @Override
        default Value<T> asValueSerializer() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Contents<T> asContentsSerializer() {
            return this;
        }

        /**
         * <p>Deserialize the the contents of the given instance from the InputStream.</p>
         *
         * @param in       the InputStream
         * @param instance the instance
         */
        void read(MCDataInput in, T instance);

        /**
         * <p>Serialize the contents of the given instance to the OutputStream.</p>
         *
         * @param out      the OutputStream
         * @param instance the instance
         */
        void write(MCDataOutput out, T instance);

        @Override
        default void write(MCDataOutput out, PropertyAccess<T> property, Object obj) {
            write(out, property.get(obj));
        }

        @Override
        default void read(MCDataInput in, PropertyAccess<T> property, Object obj) {
            read(in, property.get(obj));
        }
    }

    interface Factory extends SerializerFactory {

        @Nullable
        @Override
        <T> ByteStreamSerializer<T> getSerializer(@Nullable SerializationMethod method, TypeToken<T> type, SerializerRegistry registry);

    }

    final class Characteristics extends BaseSerializer.Characteristics {

        public Characteristics(SerializationMethod serializationMethod, boolean handlesNull) {
            super(serializationMethod, handlesNull);
        }

    }
}
