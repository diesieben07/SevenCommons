package de.take_weiland.mods.commons.sync;

import com.google.common.base.Objects;

/**
 * @author diesieben07
 */
class ImmutableAdapter<T> extends SyncAdapter<T> {

	private T value;

	@Override
	public boolean checkAndUpdate(T newValue) {
		if (!Objects.equal(newValue, value)) {
			value = newValue;
			return true;
		} else {
			return false;
		}
	}

}
