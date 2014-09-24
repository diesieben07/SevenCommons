package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;

/**
 * @author diesieben07
 */
public interface SyncedObjectProxy {

	String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncedObjectProxy";
	String READ = "_sc$sync$read";

	int _sc$sync$read(MCDataInputStream in);

}
