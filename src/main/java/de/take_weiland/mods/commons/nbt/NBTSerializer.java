package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>A serializer to encode and decode values of type {@code T} to NBT.</p>
 * @author diesieben07
 */
public interface NBTSerializer<T> {

    void read(NBTBase nbt, Consumer<? super T> setter, Supplier<? extends T> getter);

    NBTBase write(T value);

    interface ForValue<T> extends NBTSerializer<T> {

        T read(NBTBase nbt);

        @Override
        default void read(NBTBase nbt, Consumer<? super T> setter, Supplier<? extends T> getter) {
            setter.accept(read(nbt));
        }

    }

    interface ForContainer<T> extends NBTSerializer<T> {

        void read(NBTBase nbt, T value);

        @Override
        default void read(NBTBase nbt, Consumer<? super T> setter, Supplier<? extends T> getter) {
            read(nbt, getter.get());
        }
    }

}
