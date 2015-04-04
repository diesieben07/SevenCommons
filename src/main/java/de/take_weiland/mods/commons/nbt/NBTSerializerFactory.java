package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.Property;

/**
 * <p>A factory for generating NBT serializers for {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 *
* @author diesieben07
*/
public interface NBTSerializerFactory {

    <T> NBTSerializer<?> get(Property<T, ?> typeSpec);

}
