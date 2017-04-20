package de.take_weiland.mods.commons.internal.default_serializers;

import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import java.util.Collection;

/**
 * @author diesieben07
 */
abstract class CollectionInstanceSerializer<T, C extends Collection<T>> implements NBTSerializer.Value<C> {

    final Value<T> elementSerializer;

    CollectionInstanceSerializer(Value<T> elementSerializer) {
        this.elementSerializer = elementSerializer;
    }

    @Override
    public NBTBase write(C value) throws SerializationException {
        NBTTagList list = new NBTTagList();
        for (T t : value) {
            list.appendTag(t == null ? NBTData.serializedNull() : elementSerializer.write(t));
        }
        return list;
    }
}
