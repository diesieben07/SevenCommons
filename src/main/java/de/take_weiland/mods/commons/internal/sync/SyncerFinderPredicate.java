package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Predicate;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;

/**
 * @author diesieben07
 */
public final class SyncerFinderPredicate implements SyncerFinder {

	private final Predicate<SyncContext<?>> filter;
	private final ValueSyncer<?> syncer;

	public SyncerFinderPredicate(Predicate<SyncContext<?>> filter, ValueSyncer<?> syncer) {
		this.filter = filter;
		this.syncer = syncer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
		if (filter.apply(context)) {
			return (ValueSyncer<T>) syncer;
		} else {
			return null;
		}
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
		return null;
	}

	public static final class Contents implements SyncerFinder {

		private final Predicate<SyncContext<?>> filter;
		private final ContentSyncer<?> syncer;

		public Contents(Predicate<SyncContext<?>> filter, ContentSyncer<?> syncer) {
			this.filter = filter;
			this.syncer = syncer;
		}

		@Override
		public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
			if (filter.apply(context)) {
				return (ContentSyncer<T>) syncer;
			} else {
				return null;
			}
		}
	}

	public static final class Dual implements SyncerFinder {

		private final Predicate<SyncContext<?>> filter;
		private final ValueSyncer<?> valueSyncer;
		private final ContentSyncer<?> contentSyncer;

		public Dual(Predicate<SyncContext<?>> filter, ValueSyncer<?> valueSyncer, ContentSyncer<?> contentSyncer) {
			this.filter = filter;
			this.valueSyncer = valueSyncer;
			this.contentSyncer = contentSyncer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
			if (filter.apply(context)) {
				return (ValueSyncer<T>) valueSyncer;
			} else {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
			if (filter.apply(context)) {
				return (ContentSyncer<T>) contentSyncer;
			} else {
				return null;
			}
		}
	}
}
