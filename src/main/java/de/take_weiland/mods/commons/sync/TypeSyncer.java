package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface TypeSyncer<T> {

	boolean equal(T a, T b);
	
	void write(T instance, DataOutput out) throws IOException;
	
	T read(Class<? extends T> clazz, DataInput in) throws IOException;
	
}
