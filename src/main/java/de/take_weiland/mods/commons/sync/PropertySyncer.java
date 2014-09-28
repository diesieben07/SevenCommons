package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * @author diesieben07
 */
public interface PropertySyncer<T> {

	boolean hasChanged(T value);

	void writeAndUpdate(T value, MCDataOutputStream out);

	T read(T currentValue, MCDataInputStream in);

}