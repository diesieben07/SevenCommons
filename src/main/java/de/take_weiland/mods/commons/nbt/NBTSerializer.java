package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.nbt.NBTBase;

/**
 * <p>A serializer to encode and decode values of type {@code T} to NBT.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializer<T> {

    void read(NBTBase nbt, Object obj, PropertyAccess<T> property);

    default NBTBase write(Object obj, PropertyAccess<T> property) {
        T value = property.apply(obj);
        return value == null ? NBTData.serializedNull() : write(value);
    }

    NBTBase write(T value);

    interface ForValue<T> extends NBTSerializer<T> {

        T read(NBTBase nbt);

        @Override
        default void read(NBTBase nbt, Object obj, PropertyAccess<T> property) {
            if (NBTData.isSerializedNull(nbt)) {
                property.set(obj, null);
            } else {
                property.set(obj, read(nbt));
            }
        }

    }

    interface ForContainer<T> extends NBTSerializer<T> {

        void read(NBTBase nbt, T value);

        @Override
        default void read(NBTBase nbt, Object obj, PropertyAccess<T> property) {
            if (!NBTData.isSerializedNull(nbt)) {
                read(nbt, property.get(obj));
            }
        }
    }

}
