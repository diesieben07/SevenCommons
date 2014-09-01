package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;

/**
 * <p>Implement this to make your class usable with the
 * {@link de.take_weiland.mods.commons.sync.Sync @Sync} annotation.</p>
 * <p>Properties of a type implementing this interface can have the {@code @Sync} annotation.</p>
 * <p>Usually it is not required to refer to this interface other than by implementing it, but if you do,
 * use {@link de.take_weiland.mods.commons.sync.SyncableProxy} with
 * {@link de.take_weiland.mods.commons.sync.Syncing#asSyncable(SyncableProxy)} instead to also support classes that
 * implement {@link de.take_weiland.mods.commons.sync.MakeSyncable}.</p>
 *
 * @author diesieben07
 */
public interface Syncable extends SyncableProxy {

	/**
	 * <p>If this object's state has changed since the last call to {@link #writeSyncDataAndReset(de.take_weiland.mods.commons.net.MCDataOutputStream)}
	 * in a way that requires re-syncing it to the client.</p>
	 * @return true if this object needs re-syncing
	 */
	boolean needsSyncing();

	/**
	 * <p>Write this object's state to the stream. The method {@link #needsSyncing()} should return false if it's called
	 * immediately after this method and then continue to return false until this object's state changes
	 * in a way that requires re-syncing.</p>
	 * <p>The format of the data is not specified and object-specific. You can do delta updates or
	 * full updates as needed.</p>
	 * <p>The stream passed in must only be written to. <i>If</i> it's position is modified, this method must
	 * make sure that the position points to the next byte after the data written by this method before it returns.</p>
	 * @param out the stream to write your data to
	 */
	void writeSyncDataAndReset(MCDataOutputStream out);

	/**
	 * <p>Read this object's state from the stream.</p>
	 * <p>The stream contains the data in the same way it was written by {@link #writeSyncDataAndReset(de.take_weiland.mods.commons.net.MCDataOutputStream)}.</p>
	 * @param in the stream to read your data from
	 */
	void readSyncData(MCDataInputStream in);

}
