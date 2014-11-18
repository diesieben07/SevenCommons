package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;

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
public interface ValueSyncer<T> extends Syncer<T> {

	/**
	 * <p>Read a value of type T from the stream. The format is as by {@code writeAndUpdate}</p>
	 * @param in the stream
	 * @param data
	 * @return the read value
	 */
	T read(MCDataInputStream in, Object data);

}