package de.take_weiland.mods.commons.internal.tonbt.builtin;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author diesieben07
 */
final class EnumSerializer<E extends Enum<E>> implements NBTSerializer.ForValue<E> {

    private static final Map<Class<?>, NBTSerializer<?>> cache = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    static NBTSerializer<?> get(Class<?> clazz) {
        return cache.computeIfAbsent(clazz, k -> new EnumSerializer((Class) k));
    }

    private final Class<E> clazz;

    private EnumSerializer(Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public NBTBase write(@Nonnull E value) {
        return new NBTTagString(value.name());
    }

    @Override
    public E read(@Nonnull NBTBase nbt) {
        try {
            return nbt.getId() == NBT.TAG_STRING ? Enum.valueOf(clazz, ((NBTTagString) nbt).getString()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
