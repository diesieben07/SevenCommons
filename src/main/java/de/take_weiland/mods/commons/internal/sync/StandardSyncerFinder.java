package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

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
	public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
		// cast is safe, T == S
		return type.getRawType() == clazz ? (ValueSyncer<T>) syncer : null;
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
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
		public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
			// cast is safe, T == S
			return type.getRawType() == clazz ? (ContentSyncer<T>) syncer : null;
		}

	}
}
