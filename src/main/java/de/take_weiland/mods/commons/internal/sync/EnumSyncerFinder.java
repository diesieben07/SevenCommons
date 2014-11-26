package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.impl.EnumSetSyncer;

/**
 * @author diesieben07
 */
public final class EnumSyncerFinder implements SyncerFinder {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> ValueSyncer<T> findValueSyncer(TypeToken<T> type) {
		Class<? super T> raw = type.getRawType();
		if (raw.isEnum()) {
			// raw is E extends Enum<E>
			return (ValueSyncer<T>) new EnumSetSyncer<>((Class<Enum>) raw);
		} else {
			return null;
		}
	}

	@Override
	public <T> ContentSyncer<T> findContentSyncer(TypeToken<T> type) {
		return null;
	}

}
