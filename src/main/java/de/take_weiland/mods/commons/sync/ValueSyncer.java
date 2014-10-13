package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * <p>Used to add support for a type to the {@link de.take_weiland.mods.commons.sync.Sync} annotation.</p>
 * <p>A new instance of your ValueSyncer will be created for every property of type T.</p>
 *
 * <p>If your ValueSyncer can also handle subclasses of T, mark it with the {@link de.take_weiland.mods.commons.sync.HandleSubclasses}
 * interface.</p>
 * <p>ValueSyncers must be registered to the {@link de.take_weiland.mods.commons.sync.Syncing} class.</p>
 *
 * @see de.take_weiland.mods.commons.sync.Sync
 * @see de.take_weiland.mods.commons.sync.Syncing
 *
 * @author diesieben07
 */
public interface ValueSyncer<T> {

	/**
	 * <p>Determine if the value has changed since the last call to {@code writeAndUpdate}.</p>
	 * @param value the new value
	 * @return true if the value has changed
	 */
	boolean hasChanged(T value);

	/**
	 * <p>Write the given value to the stream and set it as the new, current value.</p>
	 * @param value the new value
	 * @param out the stream to write the value to
	 */
	void writeAndUpdate(T value, MCDataOutputStream out);

	/**
	 * <p>Read a value of type T from the stream. The format is as by {@code writeAndUpdate}</p>
	 * @param in the stream
	 * @return the read value
	 */
	T read(MCDataInputStream in);

}