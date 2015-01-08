package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

/**
 * @author diesieben07
 */
final class NBTSerializerRegistry extends SerializerRegistry {
    NBTSerializerRegistry() {
        super(valueSerializerIface, contentSerializerIface, annotation);
    }

    @Override
    protected Object applySPIContent(NBTSerializer.SPI spi, TypeSpecification<?> type) {
        return spi.getNBTContentSerializer(type);
    }

    @Override
    protected Object applySPIValue(NBTSerializer.SPI spi, TypeSpecification<?> type) {
        return spi.getNBTSerializer(type);
    }
}
