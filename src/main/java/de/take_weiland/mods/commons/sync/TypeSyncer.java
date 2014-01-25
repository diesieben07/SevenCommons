package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface TypeSyncer<T> {

	/**
	 * determine if the two values are equal
	 * @param now guaranteed to be non-null
	 * @param prev may be null
	 * @return
	 */
	boolean equal(T now, T prev);
	
	void write(T instance, DataOutput out) throws IOException;
	
	T read(DataInput in) throws IOException;
	
}
