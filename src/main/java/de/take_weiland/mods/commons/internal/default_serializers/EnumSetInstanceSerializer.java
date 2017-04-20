package de.take_weiland.mods.commons.internal.default_serializers;

import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.EnumSet;

/**
 * @author diesieben07
 */
final class EnumSetInstanceSerializer<E extends Enum<E>> implements NBTSerializer.Value<EnumSet<E>> {

    private final Class<E> enumClass;

    EnumSetInstanceSerializer(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Characteristics characteristics() {
        return null;
    }

    @Override
    public EnumSet<E> read(NBTBase nbt) throws SerializationException {
        EnumSet<E> set = EnumSet.noneOf(enumClass);
        NBTTagList list = (NBTTagList) nbt;
        for (int i = 0, size = list.tagCount(); i < size; i++) {
            try {
                set.add(Enum.valueOf(enumClass, ((NBTTagString) list.get(i)).getString()));
            } catch (IllegalArgumentException e) {
                // ignore invalid tags
            }
        }
        return set;
    }

    @Override
    public NBTBase write(EnumSet<E> value) throws SerializationException {
        NBTTagList list = new NBTTagList();
        for (E e : value) {
            list.appendTag(new NBTTagString(e.name()));
        }
        return list;
    }
}
