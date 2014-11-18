package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * @author diesieben07
 */
public interface Syncer<T> {

	/**
	 * <p>Determine if the value has changed since the last call to {@code writeAndUpdate}.</p>
	 * @param value the new value
	 * @param data
	 * @return true if the value has changed
	 */
	boolean hasChanged(T value, Object data);

	/**
	 * <p>Write the given value to the stream and set it as the new, current value.</p>
	 * @param value the new value
	 * @param out the stream to write the value to
	 * @param data
	 */
	Object writeAndUpdate(T value, MCDataOutputStream out, Object data);

}
