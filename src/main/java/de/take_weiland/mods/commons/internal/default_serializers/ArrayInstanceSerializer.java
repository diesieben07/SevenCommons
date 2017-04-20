package de.take_weiland.mods.commons.internal.default_serializers;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

import static com.google.common.base.Preconditions.checkNotNull;
import static de.take_weiland.mods.commons.serialize.nbt.NBTSerializerOptions.*;

/**
 * @author diesieben07
 */
final class ArrayInstanceSerializer<T> implements NBTSerializer.Value<T[]> {

    @NotNull
    static <T> NBTSerializer<T[]> create(TypeToken<T[]> type, NBTSerializer<T> elementSerializer) {
        NBTSerializer.Value<T> elementValueSerializer = elementSerializer.asValueSerializer(HANDLE_NULL, UNIFORM_NBT, IGNORE_UNKNOWN_INPUT_TAGS);
        return new ArrayInstanceSerializer<>(type, elementValueSerializer);

    }

    private static final Characteristics C = new Characteristics(SerializationMethod.VALUE, false, NBT.Tag.LIST);

    private final Class<?> elementType;
    private final NBTSerializer.Value<T> elementSerializer;

    private ArrayInstanceSerializer(TypeToken<T[]> arrayType, NBTSerializer.Value<T> elementSerializer) {
        this.elementType = checkNotNull(arrayType.getComponentType()).getRawType();
        this.elementSerializer = elementSerializer;
    }

    @Override
    public Characteristics characteristics() {
        return C;
    }

    @Override
    public final T[] read(NBTBase nbt) throws SerializationException {
        NBTTagList list = (NBTTagList) nbt;
        int size = list.tagCount();
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(elementType, size);

        for (int i = 0; i < size; i++) {
            array[i] = elementSerializer.read(list.get(i));
        }

        return array;
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
