package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

/**
 * <p>A TypeSyncer serializes and deserializes objects to a byte stream to be used with {@link Sync @Synced}.</p>
 * <p>Either register a global syncer with {@link de.take_weiland.mods.commons.sync.Syncing#registerSyncer(Class, TypeSyncer)} or define an override with
 * {@link Sync#syncer()}</p>
 */
public interface TypeSyncer<T> {

	/**
	 * determine if the two values are equal
	 * @param now guaranteed to be non-null
	 * @param prev may be null
	 * @return true if the values are considered equal
	 */
	boolean equal(T now, T prev);

	/**
	 * Write the instance given to the byte buffer
	 * @param instance the object to write, non-null
	 * @param out the buffer to write to
	 */
	void write(T instance, WritableDataBuf out);

	/**
	 * Read an instance from teh byte buffer.
	 * @param oldInstance the old object, may be null. Should be reused if possible.
	 * @param in the buffer to read from
	 * @return the deserialized object
	 */
	T read(T oldInstance, DataBuf in);
	
}
