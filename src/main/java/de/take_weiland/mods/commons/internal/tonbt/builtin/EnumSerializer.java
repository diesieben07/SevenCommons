package de.take_weiland.mods.commons.internal.tonbt.builtin;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

import java.util.HashMap;
import java.util.Map;

/**
 * @author diesieben07
 */
final class EnumSerializer<E extends Enum<E>> implements NBTSerializer<E> {

    private static final Map<Class<?>, NBTSerializer<?>> cache = new HashMap<>();

    static NBTSerializer<?> get(Class<?> clazz) {
        NBTSerializer<?> serializer = cache.get(clazz);
        if (serializer == null) {
            //noinspection unchecked,rawtypes
            serializer = new EnumSerializer<>((Class) clazz);
            cache.put(clazz, serializer);
        }
        return serializer;
    }

    private final Class<E> clazz;

    private EnumSerializer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public NBTBase write(E value) {
        return new NBTTagString("", value.name());
    }

    @Override
    public E read(E value, NBTBase nbt) {
        try {
            return nbt.getId() == NBT.TAG_STRING ? Enum.valueOf(clazz, ((NBTTagString) nbt).data) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
