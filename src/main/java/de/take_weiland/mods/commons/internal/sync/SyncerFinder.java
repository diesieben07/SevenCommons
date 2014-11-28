package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;

/**
 * @author diesieben07
 */
public interface SyncerFinder {

	<T> ValueSyncer<T> findValueSyncer(SyncContext<T> context);

	<T> ContentSyncer<T> findContentSyncer(SyncContext<T> context);

}
