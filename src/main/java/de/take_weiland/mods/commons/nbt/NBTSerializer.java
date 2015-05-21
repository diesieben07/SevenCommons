package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.nbt.NBTBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * <p>A serializer to encode and decode values of type {@code T} to NBT.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializer<T> {

    default void read(NBTBase nbt, Object obj, PropertyAccess<T> property) {
        read(nbt, obj, property, property);
    }

    void read(NBTBase nbt, Object obj, Function<Object, ? extends T> getter, BiConsumer<Object, ? super T> setter);

    default NBTBase write(Object obj, PropertyAccess<T> property) {
        return write(obj, property, property);
    }

    default NBTBase write(Object obj, Function<Object, ? extends T> getter, BiConsumer<Object, ? super T> setter) {
        T value = getter.apply(obj);
        return value == null ? NBTData.serializedNull() : write(value);
    }

    NBTBase write(T value);

    interface ForValue<T> extends NBTSerializer<T> {

        T read(NBTBase nbt);

        @Override
        default void read(NBTBase nbt, Object obj, Function<Object, ? extends T> getter, BiConsumer<Object, ? super T> setter) {
            if (NBTData.isSerializedNull(nbt)) {
                setter.accept(obj, null);
            } else {
                setter.accept(obj, read(nbt));
            }
        }

    }

    interface ForContainer<T> extends NBTSerializer<T> {

        void read(NBTBase nbt, T value);

        @Override
        default void read(NBTBase nbt, Object obj, Function<Object, ? extends T> getter, BiConsumer<Object, ? super T> setter) {
            if (!NBTData.isSerializedNull(nbt)) {
                read(nbt, getter.apply(obj));
            }
        }
    }

}
