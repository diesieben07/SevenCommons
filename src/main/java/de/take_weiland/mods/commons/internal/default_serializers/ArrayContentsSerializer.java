package de.take_weiland.mods.commons.internal.default_serializers;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import static de.take_weiland.mods.commons.serialize.nbt.NBTSerializerOptions.*;

/**
 * @author diesieben07
 */
final class ArrayContentsSerializer<T> implements NBTSerializer.Contents<T[]> {

    static <T> NBTSerializer.Contents<T[]> create(NBTSerializer<T> elementSerializer) {
        NBTSerializer.Contents<T> elementContentsSerializer = elementSerializer.asContentsSerializer(HANDLE_NULL, UNIFORM_NBT, IGNORE_UNKNOWN_INPUT_TAGS);
        return new ArrayContentsSerializer<>(elementContentsSerializer);
    }

    private static final Characteristics C = new Characteristics(SerializationMethod.CONTENTS, false, NBT.Tag.LIST);

    private final NBTSerializer.Contents<T> elementSerializer;

    private ArrayContentsSerializer(NBTSerializer.Contents<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    @Override
    public Characteristics characteristics() {
        return C;
    }

    @Override
    public final void read(NBTBase nbt, T[] value) throws SerializationException {
        NBTTagList list = (NBTTagList) nbt;
        int size = Math.min(value.length, list.tagCount());

        for (int i = 0; i < size; i++) {
            elementSerializer.read(list.get(i), value[i]);
        }
    }

    @Override
    public final NBTBase write(T[] value) throws SerializationException {
        NBTTagList list = new NBTTagList();

        for (T t : value) {
            list.appendTag(elementSerializer.write(t));
        }

        return list;
    }
}
