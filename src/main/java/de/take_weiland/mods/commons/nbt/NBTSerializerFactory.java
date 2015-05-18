package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.Property;

/**
 * <p>A factory for generating NBT serializers for {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 *
 * @author diesieben07
 */
public interface NBTSerializerFactory {

    /**
     * <p>Get a {@code NBTSerializer} for the given property. If this factory cannot provide a serializer for
     * the property, {@code null} should be returned.</p>
     * @param property the property
     * @return an NBTSerializer
     */
    <T> NBTSerializer<?> get(Property<T, ?> property);

}
