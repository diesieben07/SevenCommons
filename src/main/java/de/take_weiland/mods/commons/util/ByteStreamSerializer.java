package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	void write(T instance, MCDataOutputStream out);

	T read(MCDataInputStream in);

}
