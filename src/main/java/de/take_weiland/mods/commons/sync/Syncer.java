package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * <p>Base interface for {@link de.take_weiland.mods.commons.sync.ValueSyncer} and {@link de.take_weiland.mods.commons.sync.ContentSyncer}.
 * This interfaces should not be implemented directly.</p>
 * <p>Syncers have singleton character. There usually is only one Syncer for all instances of a type.</p>
 * <p>To allow changes to be detected in the watched objects, there is a "data object" associated with each property that
 * needs to be synced. It can be any value, but will initially be {@code null}. It can be changed by returning something different
 * in {@link #writeAndUpdate(Object, de.take_weiland.mods.commons.net.MCDataOutputStream, Object)}.</p>
 * @author diesieben07
 */
public interface Syncer<T> {

	/**
	 * <p>Determine if the value has changed since the last call to {@code writeAndUpdate}.</p>
	 * @param value the new value
	 * @param data the data object
	 * @return true if the value has changed
	 */
	boolean hasChanged(T value, Object data);

	/**
	 * <p>Write the given value to the stream and set it as the new, current value.</p>
	 * @param value the new value
	 * @param out the stream to write the value to
	 * @param data the data object
	 */
	Object writeAndUpdate(T value, MCDataOutputStream out, Object data);

}
