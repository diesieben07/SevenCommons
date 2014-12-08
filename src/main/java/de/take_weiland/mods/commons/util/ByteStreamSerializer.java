package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

import javax.annotation.Nonnull;

/**
 * <p>A serializer for objects of type {@code T}.</p>
 * <p>Implementations may or may not support null values.</p>
 *
 *
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	/**
	 * <p>Serialize the given instance to the stream.</p>
	 * @param instance the instance to serialize
	 * @param out the stream
	 */
	void write(T instance, @Nonnull MCDataOutputStream out);

	/**
	 * <p>Deserialize an instance from the stream.</p>
	 * @param in the stream
	 * @return the deserialized instance
	 */
	T read(MCDataInputStream in);

	public interface Contents<T> {

		void write(@Nonnull T instance, @Nonnull MCDataOutputStream out);

		void read(@Nonnull T instance, @Nonnull MCDataInputStream in);

	}

}
