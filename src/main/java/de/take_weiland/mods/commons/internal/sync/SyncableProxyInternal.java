package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.Syncable;

/**
 * @author diesieben07
 */
public interface SyncableProxyInternal {

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncableProxyInternal";
	String NEEDS_SYNCING = "_sc$syncable$needsSyncing";
	String NEEDS_SYNCING_ORIG = "needsSyncing";
	String WRITE = "_sc$syncable$write";
	String WRITE_ORIG = "writeSyncDataAndReset";
	String READ = "_sc$syncable$read";
	String READ_ORIG = "readSyncData";

	String AS_SYNCABLE = "_sc$asSyncable";

	boolean _sc$syncable$needsSyncing();

	void _sc$syncable$write(MCDataOutputStream out);

	void _sc$syncable$read(MCDataInputStream in);

	Syncable _sc$asSyncable();

}
