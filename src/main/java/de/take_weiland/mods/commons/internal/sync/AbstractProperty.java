package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.sync.SyncableProperty;

/**
 * @author diesieben07
 */
public abstract class AbstractProperty implements SyncableProperty<Object> {

	private Object data;

	@Override
	public Object getData() {
		return data;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

}
