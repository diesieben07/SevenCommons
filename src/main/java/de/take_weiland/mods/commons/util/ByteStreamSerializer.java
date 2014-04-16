package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializer<T> {

	void write(T instance, WritableDataBuf out);

	T read(DataBuf buf);

}
