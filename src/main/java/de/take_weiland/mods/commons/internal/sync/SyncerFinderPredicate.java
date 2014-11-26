package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
public final class SyncerFinderPredicate implements SyncerFinder {

	private final Predicate<TypeToken<?>> filter;
	private final ValueSyncer<?> syncer;

	public SyncerFinderPredicate(Predicate<TypeToken<?>> filter, ValueSyncer<?> syncer) {
		this.filter = filter;
		this.syncer = syncer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
		if (filter.apply(type)) {
			return (ValueSyncer<T>) syncer;
		} else {
			return null;
		}
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
		return null;
	}

	public static final class Contents implements SyncerFinder {

		private final Predicate<TypeToken<?>> filter;
		private final ContentSyncer<?> syncer;

		public Contents(Predicate<TypeToken<?>> filter, ContentSyncer<?> syncer) {
			this.filter = filter;
			this.syncer = syncer;
		}

		@Override
		public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
			if (filter.apply(type)) {
				return (ContentSyncer<T>) syncer;
			} else {
				return null;
			}
		}
	}
}
