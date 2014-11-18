package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 *
 * @author diesieben07
 */
public interface ContentSyncer<T> {

	boolean hasChanged(T value, Object data);

	Object writeAndUpdate(T value, MCDataOutputStream out, Object data);

	void read(T value, MCDataInputStream in, Object data);

}
