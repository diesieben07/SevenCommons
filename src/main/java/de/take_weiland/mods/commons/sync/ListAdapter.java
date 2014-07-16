package de.take_weiland.mods.commons.sync;

import java.util.Arrays;
import java.util.List;

/**
 * @author diesieben07
 */
abstract class ListAdapter<V, LIST extends List<V>> extends SyncAdapter<LIST> {

	private final AdapterCreator<? super V> valueAdapter;

	ListAdapter(AdapterCreator<? super V> valueAdapter) {
		this.valueAdapter = valueAdapter;
	}

	SyncAdapter<? super V>[] adapters;

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkAndUpdate(LIST newValue) {
		if (newValue == null) {
			if (adapters == null) {
				return false;
			} else {
				adapters = null;
				return true;
			}
		} else {
			int newLen = newValue.size();
			if (adapters == null) {
				SyncAdapter<? super V>[] adapters;
				adapters = this.adapters = new SyncAdapter[newLen];
				for (int i = 0; i < newLen; ++i) {
					adapters[i] = valueAdapter.newInstance();
				}
			} else {
				int myLen = adapters.length;
				if (newLen < myLen) {
					adapters = Arrays.copyOf(adapters, newLen);
				} else if (newLen > myLen) {
					SyncAdapter<? super V>[] adapters = this.adapters = Arrays.copyOf(this.adapters, newLen);
					AdapterCreator<? super V> valueAdapter = this.valueAdapter;
					for (int i = myLen; i < newLen; ++i) {
						adapters[i] = valueAdapter.newInstance();
					}
				}
			}


			return iterativeCheck(newValue);
		}
	}

	abstract boolean iterativeCheck(LIST list);

}
