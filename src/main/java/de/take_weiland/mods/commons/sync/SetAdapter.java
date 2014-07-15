package de.take_weiland.mods.commons.sync;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author diesieben07
 */
abstract class SetAdapter<IT extends Set<V>, V> extends SyncAdapter<IT> {

	final InstanceCreator<V> valueAdapter;

	SetAdapter(InstanceCreator<V> valueAdapter) {
		this.valueAdapter = valueAdapter;
	}

	static class Sorted<IT extends SortedSet<V>, V> extends SetAdapter<IT, V> {

		SyncAdapter<? super V>[] values;

		Sorted(InstanceCreator<V> valueAdapter) {
			super(valueAdapter);
		}

		@Override
		public boolean checkAndUpdate(IT newValue) {
			if (newValue == null) {

			}
		}
	}

}
