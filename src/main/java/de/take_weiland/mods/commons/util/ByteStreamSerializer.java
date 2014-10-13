package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * <p>A serializer for objects of type {@code T}.</p>
 * <p>Implementations may or may not support null values.</p>
 *
 * @see de.take_weiland.mods.commons.util.ByteStreamSerializable
 * @see ByteStreamSerializers
 *
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	/**
	 * <p>Serialize the given instance to the stream.</p>
	 * @param instance the instance to serialize
	 * @param out the stream
	 */
	void write(T instance, MCDataOutputStream out);

	/**
	 * <p>Deserialize an instance from the stream.</p>
	 * @param in the stream
	 * @return the deserialized instance
	 */
	T read(MCDataInputStream in);

}
