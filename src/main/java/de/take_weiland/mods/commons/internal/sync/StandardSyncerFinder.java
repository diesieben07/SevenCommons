package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;

/**
 * @author diesieben07
 */
public final class StandardSyncerFinder<S> implements SyncerFinder {

	private final Class<S> clazz;
	private final ValueSyncer<S> syncer;

	public StandardSyncerFinder(Class<S> clazz, ValueSyncer<S> syncer) {
		this.clazz = clazz;
		this.syncer = syncer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
		// cast is safe, T == S
		return context.getRawType() == clazz ? (ValueSyncer<T>) syncer : null;
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
		return null;
	}

	public static final class Content<S> implements SyncerFinder {

		private final Class<S> clazz;
		private final ContentSyncer<S> syncer;

		public Content(Class<S> clazz, ContentSyncer<S> syncer) {
			this.clazz = clazz;
			this.syncer = syncer;
		}

		@Override
		public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
			// cast is safe, T == S
			return context.getRawType() == clazz ? (ContentSyncer<T>) syncer : null;
		}

	}
}
