package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

public interface TypeSyncer<T> {

	/**
	 * determine if the two values are equal
	 * @param now guaranteed to be non-null
	 * @param prev may be null
	 * @return
	 */
	boolean equal(T now, T prev);
	
	void write(T instance, WritableDataBuf out);
	
	T read(T oldInstance, DataBuf in);
	
}
