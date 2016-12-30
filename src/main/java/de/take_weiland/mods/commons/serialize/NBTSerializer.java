package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import net.minecraft.nbt.NBTBase;

/**
 * <p>A serializer that can serialize values of type {@code T} to and from NBT.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializer<T> extends BaseSerializer<T> {

    void read(PropertyAccess<T> property, Object obj, NBTBase nbt);

    NBTBase write(PropertyAccess<T> property, Object obj);

    /**
     * <p>A NBTSerializer for types where de-serialization produces a new instance.
     * An example is String.</p>
     *
     */
    interface Instance<T> extends NBTSerializer<T> {

        T read(NBTBase nbt);

        NBTBase write(T value);

        @Override
        default void read(PropertyAccess<T> property, Object obj, NBTBase nbt) {
            if (NBTData.isSerializedNull(nbt)) {
                property.set(obj, null);
            } else {
                property.set(obj, read(nbt));
            }
        }

        @Override
        default NBTBase write(PropertyAccess<T> property, Object obj) {
            T v = property.get(obj);
            if (v == null) {
                return NBTData.serializedNull();
            } else {
                return write(v);
            }
        }
    }

    /**
     * <p>A NBTSerializer for types where de-serialization changes the contents of an existing instance.
     * An example is FluidTank.</p>
     */
    interface Contents<T> extends NBTSerializer<T> {

        void read(NBTBase nbt, T value);

        NBTBase write(T value);

        @Override
        default void read(PropertyAccess<T> property, Object obj, NBTBase nbt) {
            read(nbt, property.get(obj));
        }

        @Override
        default NBTBase write(PropertyAccess<T> property, Object obj) {
            return write(property.get(obj));
        }

    }

}
