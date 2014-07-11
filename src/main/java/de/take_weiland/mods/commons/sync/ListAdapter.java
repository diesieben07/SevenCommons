package de.take_weiland.mods.commons.sync;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author diesieben07
 */
abstract class ListAdapter<V, LIST extends List<V>> extends SyncAdapter<LIST> {

	private final InstanceCreator<SyncAdapter<? super V>> valueAdapter;

	ListAdapter(InstanceCreator<SyncAdapter<? super V>> valueAdapter) {
		this.valueAdapter = valueAdapter;
	}

	SyncAdapter<? super V>[] adapters;
	private int actualLength;

	@Override
	public boolean checkAndUpdate(LIST newValue) {
		if (newValue == null) {
			return actualLength != -1;
		} else {
			int newLen = newValue.size();
			int myLen = actualLength;
			if (newLen < myLen) {
				adapters = Arrays.copyOf(adapters, newLen);
			} else if (newLen > myLen) {
				SyncAdapter<? super V>[] adapters = this.adapters = Arrays.copyOf(this.adapters, newLen);
				InstanceCreator<SyncAdapter<? super V>> valueAdapter = this.valueAdapter;
				for (int i = myLen; i < newLen; ++i) {
					adapters[i] = valueAdapter.newInstance();
				}
			}

			return iterativeCheck(newValue);
		}
	}

	abstract boolean iterativeCheck(List<V> list);

	static class ForLinked<V, LIST extends List<V>> extends ListAdapter<V, LIST> {
		ForLinked(InstanceCreator<SyncAdapter<? super V>> valueAdapter) {
			super(valueAdapter);
		}

		@Override
		boolean iterativeCheck(List<V> list) {
			Iterator<V> it = list.iterator();
			SyncAdapter<? super V>[] adapters = this.adapters;
			boolean changed = false;
			for (int i = 0; it.hasNext(); ++i) {
				changed |= adapters[i].checkAndUpdate(it.next());
			}
			return changed;
		}

	}

	// specialized for RandomAccess lists to avoid iterators
	static class ForRandomAccess<V, LIST extends List<V>> extends ListAdapter<V, LIST> {

		ForRandomAccess(InstanceCreator<SyncAdapter<? super V>> valueAdapter) {
			super(valueAdapter);
		}

		@Override
		boolean iterativeCheck(List<V> list) {
			SyncAdapter<? super V>[] adapters = this.adapters;
			boolean changed = false;
			int len = list.size();
			for (int i = 0; i < len; ++i) {
				changed |= adapters[i].checkAndUpdate(list.get(i));
			}
			return changed;
		}

	}
}
