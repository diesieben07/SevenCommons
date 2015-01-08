package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.ByteStreamSerializer;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

/**
 * @author diesieben07
 */
public final class ByteStreamSerializerRegistry extends SerializerRegistry {
	public ByteStreamSerializerRegistry() {
		super(valueSerializerIface, contentSerializerIface, annotation);
	}

	@Override
	protected Object applySPIContent(ByteStreamSerializer.SPI spi, TypeSpecification<?> type) {
		return spi.getContentStreamSerializer(type);
	}

	@Override
	protected Object applySPIValue(ByteStreamSerializer.SPI spi, TypeSpecification<?> type) {
		return spi.getStreamSerializer(type);
	}
}
