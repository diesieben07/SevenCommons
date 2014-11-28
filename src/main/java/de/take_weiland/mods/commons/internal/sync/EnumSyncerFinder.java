package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.sync.impl.EnumSyncer;

import java.util.concurrent.ConcurrentMap;

/**
 * @author diesieben07
 */
public final class EnumSyncerFinder implements SyncerFinder {

	private static ConcurrentMap<Class<?>, EnumSyncer<?>> cache;

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
		Class<?> clazz = context.getRawType();
		if (clazz.isEnum()) {
			ConcurrentMap<Class<?>, EnumSyncer<?>> cache = getCache();
			EnumSyncer<?> syncer = cache.get(clazz);
			if (syncer == null) {
				syncer = new EnumSyncer<>((Class<Enum>) clazz);
				if (cache.putIfAbsent(clazz, syncer) != null) {
					syncer = cache.get(clazz);
				}
			}

			// clazz is E extends Enum<E>
			return (ValueSyncer<T>) syncer;
		} else {
			return null;
		}
	}

	private static ConcurrentMap<Class<?>, EnumSyncer<?>> getCache() {
		if (cache == null) {
			synchronized (EnumSyncerFinder.class) {
				if (cache == null) {
					cache = new MapMaker().concurrencyLevel(2).makeMap();
				}
			}
		}
		return cache;
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
		return null;
	}

}
