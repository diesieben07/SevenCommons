package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
public interface SyncerFinder {

	<T> ValueSyncer<T> findValueSyncer(TypeToken<T> type);

	<T> ContentSyncer<T> findContentSyncer(TypeToken<T> type);

}
