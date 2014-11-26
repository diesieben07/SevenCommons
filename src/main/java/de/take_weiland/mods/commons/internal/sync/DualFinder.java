package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
public final class DualFinder<T> implements SyncerFinder {

	private final Class<T> clazz;
	private final ValueSyncer<T> valueSyncer;
	private final ContentSyncer<T> contentSyncer;

	public DualFinder(Class<T> clazz, ValueSyncer<T> valueSyncer, ContentSyncer<T> contentSyncer) {
		this.clazz = clazz;
		this.valueSyncer = valueSyncer;
		this.contentSyncer = contentSyncer;
	}

	@Override
	public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
		return null;
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
		return null;
	}
}
