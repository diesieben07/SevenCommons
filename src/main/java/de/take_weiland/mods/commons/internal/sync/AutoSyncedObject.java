package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;

/**
 * @author diesieben07
 */
public interface AutoSyncedObject {

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/AutoSyncedObject";
	String SYNC_READ = "_sc$sync$read";

	int _sc$sync$read(MCDataInputStream stream, boolean isSuperCall);

}
