package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.SyncableProxyInternal;

/**
 * <p>Utility class for working with {@link de.take_weiland.mods.commons.sync.Syncable Syncable} objects.</p>
 *
 * @author diesieben07
 */
public final class Syncing {

	/**
	 * <p>Create a {@code Syncable} from a {@code SyncableProxy}.</p>
	 * @param syncable the proxy
	 * @return the corresponding Syncable instance
	 */
	public static Syncable asSyncable(SyncableProxy syncable) {
		return ((SyncableProxyInternal) syncable)._sc$asSyncable();
	}

	private Syncing() { }

}
