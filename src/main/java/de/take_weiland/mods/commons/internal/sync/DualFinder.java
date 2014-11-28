package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;

/**
 * @author diesieben07
 */
public final class DualFinder<R> implements SyncerFinder {

	private final Class<R> clazz;
	private final ValueSyncer<R> valueSyncer;
	private final ContentSyncer<R> contentSyncer;

	public DualFinder(Class<R> clazz, ValueSyncer<R> valueSyncer, ContentSyncer<R> contentSyncer) {
		this.clazz = clazz;
		this.valueSyncer = valueSyncer;
		this.contentSyncer = contentSyncer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
		if (context.getRawType() == clazz) {
			// cast is safe, R == T
			return (ValueSyncer<T>) valueSyncer;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
		if (context.getRawType() == clazz) {
			return (ContentSyncer<T>) contentSyncer;
		} else {
			return null;
		}
	}
}
