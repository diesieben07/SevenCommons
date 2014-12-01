package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.sync.impl.EnumSyncer;

/**
 * @author diesieben07
 */
public final class EnumSyncerFinder implements SyncerFinder {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> ValueSyncer<T> findValueSyncer(SyncContext<T> context) {
		Class<?> clazz = context.getRawType();
		if (clazz.isEnum()) {
			return (ValueSyncer<T>) new EnumSyncer<>((Class<Enum>) clazz);
		} else {
			return null;
		}
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(SyncContext<T> context) {
		return null;
	}

}
