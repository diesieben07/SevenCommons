package de.take_weiland.mods.commons.serialize.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.serialize.*;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * <p>A serializer that can serialize values of type {@code T} to and from NBT.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializer<T> extends BaseSerializer<T> {

    default Value<T> asValueSerializer() {
        return new NBTValueSerializerShim<>(this);
    }

    default NBTSerializer.Value<T> asValueSerializer(NBTSerializerOptions... options) {
        return NBTSerializerWrapper.create(this, ImmutableSet.copyOf(options)).asValueSerializer();
    }

    default NBTSerializer.Contents<T> asContentsSerializer() {
        return new NBTContentsSerializerShim<>(this);
    }

    default NBTSerializer.Contents<T> asContentsSerializer(NBTSerializerOptions... options) {
        return NBTSerializerWrapper.create(this, ImmutableSet.copyOf(options)).asContentsSerializer();
    }

    @Override
    Characteristics characteristics();

    void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException;

    NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException;

    /**
     * <p>A NBTSerializer for types where de-serialization produces a new instance.
     * An example is String.</p>
     */
    interface Value<T> extends NBTSerializer<T> {

        @Override
        default Value<T> asValueSerializer() {
            return this;
        }

        @Override
        default Contents<T> asContentsSerializer() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Contents<T> asContentsSerializer(NBTSerializerOptions... options) {
            throw new UnsupportedOperationException();
        }

        T read(NBTBase nbt) throws SerializationException;

        NBTBase write(T value) throws SerializationException;

        @Override
        default void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException {
            property.set(obj, read(nbt));
        }

        @Override
        default NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException {
            return write(property.get(obj));
        }
    }

    /**
     * <p>A NBTSerializer for types where de-serialization changes the contents of an existing instance.
     * An example is FluidTank.</p>
     */
    interface Contents<T> extends NBTSerializer<T> {

        @Override
        default Value<T> asValueSerializer() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Value<T> asValueSerializer(NBTSerializerOptions... options) {
            throw new UnsupportedOperationException();
        }

        @Override
        default Contents<T> asContentsSerializer() {
            return this;
        }

        void read(NBTBase nbt, T value) throws SerializationException;

        NBTBase write(T value) throws SerializationException;

        @Override
        default void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException {
            read(nbt, property.get(obj));
        }

        @Override
        default NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException {
            return write(property.get(obj));
        }

    }

    interface Factory extends SerializerFactory {

        @Nullable
        @Override
        <T> NBTSerializer<T> getSerializer(@Nullable SerializationMethod method, TypeToken<T> type, SerializerRegistry registry);

    }

    final class Characteristics extends BaseSerializer.Characteristics {

        private final Set<NBT.Tag<?>> supportedInputTags;
        private final Set<NBT.Tag<?>> outputTags;

        public Characteristics(SerializationMethod serializationMethod, boolean handlesNull, NBT.Tag<?> nbtType) {
            this(serializationMethod, handlesNull, Collections.singleton(nbtType));
        }

        public Characteristics(SerializationMethod serializationMethod, boolean handlesNull, Iterable<NBT.Tag<?>> supportedTags) {
            this(serializationMethod, handlesNull, supportedTags, supportedTags);
        }

        public Characteristics(SerializationMethod serializationMethod, boolean handlesNull, Iterable<NBT.Tag<?>> supportedInputTags, Iterable<NBT.Tag<?>> outputTags) {
            super(serializationMethod, handlesNull);
            this.supportedInputTags = ImmutableSet.copyOf(supportedInputTags);
            ImmutableSet<NBT.Tag<?>> temp = ImmutableSet.copyOf(outputTags);
            if (temp.equals(this.supportedInputTags)) {
                this.outputTags = this.supportedInputTags;
            } else {
                this.outputTags = temp;
            }

            if (this.supportedInputTags.isEmpty() || this.outputTags.isEmpty()) {
                throw new IllegalArgumentException("Must specify at least one supported input tag and one output tag.");
            }
        }

        /**
         * <p>The tag types that may be passed into the serializer. If a tag type is in this set it merely means that the
         * serializer can handle that type in a specified manner, it does not mean it will produce a meaningful output.</p>
         * <p>Tags not in this set must never be passed into the serializer.</p>
         *
         * @return the set of tag types
         */
        public Set<NBT.Tag<?>> getSupportedInputTags() {
            return supportedInputTags;
        }

        /**
         * <p>The tag types that this serializer will produce.</p>
         *
         * @return the set of tag types
         */
        public Set<NBT.Tag<?>> getOutputTags() {
            return outputTags;
        }

    }

}
