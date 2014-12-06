package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;

/**
 * <p>A syncer that synchronizes the <i>contents</i> of mutable objects. The object itself will never change.</p>
 * @author diesieben07
 */
public interface ContentSyncer<T> extends Syncer<T> {

	/**
	 * <p>Update the value from the stream.</p>
	 * @param value the object to be updated
	 * @param in the InputStream
	 * @param data the data object
	 */
	void read(T value, MCDataInputStream in, Object data);

}
