package de.take_weiland.mods.commons.serialize;

import de.take_weiland.mods.commons.properties.RichType;

/**
 * @author diesieben07
 */
public interface SerializerProvider {

	<T> ByteStreamSerializer<T> get(RichType<T> type);

}
