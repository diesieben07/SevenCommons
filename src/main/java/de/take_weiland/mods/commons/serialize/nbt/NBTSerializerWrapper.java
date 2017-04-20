package de.take_weiland.mods.commons.serialize.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import net.minecraft.nbt.NBTBase;

import java.util.Set;

/**
 * @author diesieben07
 */
abstract class NBTSerializerWrapper<T, D extends NBTSerializer<T>> implements NBTSerializer<T> {

    private static final short
            HANDLE_NULL = 1 << 12,
            WRAP = 1 << 13,
            THROW_ON_INVALID_TAG = 1 << 14,
            INVALID_TAGS_TO_NULL = (short) (1 << 15);

    static <T> NBTSerializer<T> create(NBTSerializer<T> delegate, Set<NBTSerializerOptions> options) {
        Characteristics delegateCharacteristics = delegate.characteristics();
        SerializationMethod serializationMethod = delegateCharacteristics.getSerializationMethod();

        // we need to handle null if it was requested and the delegate does not handle it already.
        boolean handleNull = options.contains(NBTSerializerOptions.HANDLE_NULL) && !delegateCharacteristics.handlesNull();

        boolean unknownInputTagsToNull = options.contains(NBTSerializerOptions.UNKNOWN_INPUT_TAGS_TO_NULL);
        boolean prohibitUnknownInputTags = options.contains(NBTSerializerOptions.PROHIBIT_UNKNOWN_INPUT_TAGS);
        boolean ignoreUnknownInputTags = options.contains(NBTSerializerOptions.IGNORE_UNKNOWN_INPUT_TAGS);

        // 1. if uniform NBT wasn't requested, no need to wrap.
        // 2. if we do not have to handle null and the delegate only produces one output tag, no need to wrap.
        // 3. if the delegate only produces NBTTagCompound, no need to wrap, even if we have to handle null
        boolean wrap = options.contains(NBTSerializerOptions.UNIFORM_NBT)
                && !(!handleNull && delegateCharacteristics.getOutputTags().size() == 1)
                && !delegateCharacteristics.getOutputTags().equals(ImmutableSet.of(NBT.Tag.COMPOUND));

        NBTSerializer<T> result;

        if ((!handleNull || serializationMethod == SerializationMethod.CONTENTS) && !wrap && !unknownInputTagsToNull && !prohibitUnknownInputTags && !ignoreUnknownInputTags) {
            // we don't need to do anything, just return the serializer
            result = serializationMethod == SerializationMethod.CONTENTS ? delegate.asContentsSerializer() : delegate.asValueSerializer();
        } else {
            // we always handle any tag
            Set<NBT.Tag<?>> myInputTags = NBT.Tag.ALL;

            Set<NBT.Tag<?>> myOutputTags;
            if (wrap) {
                myOutputTags = ImmutableSet.of(NBT.Tag.COMPOUND);
            } else if (handleNull) {
                myOutputTags = Sets.union(ImmutableSet.of(NBT.Tag.COMPOUND), delegate.characteristics().getOutputTags()).immutableCopy();
            } else {
                myOutputTags = ImmutableSet.copyOf(delegate.characteristics().getOutputTags());
            }

            Characteristics myCharacteristics = new Characteristics(SerializationMethod.VALUE, handleNull || delegate.characteristics().handlesNull(), myInputTags, myOutputTags);

            short flags = (short) ((handleNull ? HANDLE_NULL : 0)
                    | (wrap ? WRAP : 0)
                    | (unknownInputTagsToNull ? INVALID_TAGS_TO_NULL : 0)
                    | (prohibitUnknownInputTags ? THROW_ON_INVALID_TAG : 0));

            for (NBT.Tag<?> tag : delegate.characteristics().getSupportedInputTags()) {
                flags |= 1 << tag.id();
            }

            if (serializationMethod == SerializationMethod.VALUE) {
                result = new NBTSerializerWrapper.Value<>(delegate.asValueSerializer(), flags, myCharacteristics);
            } else {
                result = new NBTSerializerWrapper.Contents<>(delegate.asContentsSerializer(), flags, myCharacteristics);
            }
        }
        return result;
    }

    final D delegate;
    private final int flags;
    private final Characteristics c;

    NBTSerializerWrapper(D delegate, int flags, Characteristics c) {
        this.delegate = delegate;
        this.flags = flags;
        this.c = c;
    }

    final SerializationException newInvalidTagException(NBTBase nbt) {
        return new SerializationException("Invalid input tag type: " + NBT.Tag.byId(nbt.getId()));
    }

    final boolean hasFlag(int flag) {
        return (flags & flag) != 0;
    }

    final boolean isValidInputTagForDelegate(NBTBase nbt) {
        return (flags & (1 << nbt.getId())) != 0;
    }

    @Override
    public Characteristics characteristics() {
        return c;
    }

    @Override
    public void read(NBTBase nbt, PropertyAccess<T> property, Object obj) throws SerializationException {
        if (hasFlag(HANDLE_NULL) && NBTData.isSerializedNull(nbt)) {
            handleNull(property, obj);
        } else {
            if (hasFlag(WRAP)) {
                nbt = NBTData.unwrap(nbt);
            }
            if (!isValidInputTagForDelegate(nbt)) {
                if (hasFlag(THROW_ON_INVALID_TAG)) {
                    throw newInvalidTagException(nbt);
                } else if (hasFlag(INVALID_TAGS_TO_NULL)) {
                    property.set(obj, null);
                }
                // else ignore
            } else {
                delegate.read(nbt, property, obj);
            }
        }
    }

    abstract void handleNull(PropertyAccess<T> property, Object obj) throws SerializationException;

    @Override
    public NBTBase write(PropertyAccess<T> property, Object obj) throws SerializationException {
        if (hasFlag(HANDLE_NULL) && property.get(obj) == null) {
            return NBTData.serializedNull();
        } else {
            NBTBase nbt = delegate.write(property, obj);
            return maybeWrap(nbt);
        }
    }

    NBTBase maybeWrap(NBTBase nbt) {
        if (hasFlag(WRAP)) {
            return NBTData.serializedWrapper(nbt);
        } else {
            return nbt;
        }
    }

    private static final class Value<T> extends NBTSerializerWrapper<T, NBTSerializer.Value<T>> implements NBTSerializer.Value<T> {

        Value(NBTSerializer.Value<T> delegate, int flags, Characteristics c) {
            super(delegate, flags, c);
        }

        @Override
        public NBTSerializer.Value<T> asValueSerializer(NBTSerializerOptions... options) {
            return delegate.asValueSerializer(options);
        }

        @Override
        void handleNull(PropertyAccess<T> property, Object obj) throws SerializationException {
            property.set(obj, null);
        }

        @Override
        public T read(NBTBase nbt) throws SerializationException {
            if (hasFlag(HANDLE_NULL) && NBTData.isSerializedNull(nbt)) {
                return null;
            } else {
                if (hasFlag(WRAP)) {
                    nbt = NBTData.unwrap(nbt);
                }
                if (!isValidInputTagForDelegate(nbt)) {
                    if (hasFlag(THROW_ON_INVALID_TAG)) {
                        throw newInvalidTagException(nbt);
                    } else {
                        return null;
                    }
                } else {
                    return delegate.read(nbt);
                }
            }
        }

        @Override
        public NBTBase write(T value) throws SerializationException {
            if (hasFlag(HANDLE_NULL) && value == null) {
                return NBTData.serializedNull();
            } else {
                NBTBase nbt = delegate.write(value);
                return maybeWrap(nbt);
            }
        }
    }

    private static final class Contents<T> extends NBTSerializerWrapper<T, NBTSerializer.Contents<T>> implements NBTSerializer.Contents<T> {

        Contents(NBTSerializer.Contents<T> delegate, int flags, Characteristics c) {
            super(delegate, flags, c);
        }

        @Override
        public NBTSerializer.Contents<T> asContentsSerializer(NBTSerializerOptions... options) {
            return delegate.asContentsSerializer(options);
        }

        @Override
        void handleNull(PropertyAccess<T> property, Object obj) throws SerializationException {
            // ignore
        }

        @Override
        public void read(NBTBase nbt, T value) throws SerializationException {
            if (hasFlag(WRAP)) {
                nbt = NBTData.unwrap(nbt);
            }
            if (!isValidInputTagForDelegate(nbt)) {
                if (hasFlag(THROW_ON_INVALID_TAG)) {
                    throw newInvalidTagException(nbt);
                }
            } else {
                delegate.read(nbt, value);
            }
        }

        @Override
        public NBTBase write(T value) throws SerializationException {
            NBTBase nbt = delegate.write(value);
            return maybeWrap(nbt);
        }

    }

}
