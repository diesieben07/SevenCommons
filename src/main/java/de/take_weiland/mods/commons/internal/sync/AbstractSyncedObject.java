package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Objects;

public abstract class AbstractSyncedObject<T> implements SyncedField {

	protected T current;
	protected T last;
	
	@Override
	public boolean needsSyncing() {
		return !Objects.equal(current, last);
	}

	@Override
	public void refreshed() {
		last = current;
	}

}
