package de.take_weiland.mods.commons.internal.updater;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.util.Comparator;

/**
 * @author diesieben07
 */
public final class ModSorter {

	public static final Ordering<UpdatableMod> INSTANCE;

	static {
		INSTANCE = Ordering.from(new Comparator<UpdatableMod>() {
			@Override
			public int compare(UpdatableMod o1, UpdatableMod o2) {
				boolean isInternal1 = o1.isInternal();
				boolean isInternal2 = o2.isInternal();
				if (isInternal1) {
					return isInternal2 ? 0 : 1;
				} else {
					return !isInternal2 ? 0 : -1;
				}
			}
		}).compound(Ordering.from(String.CASE_INSENSITIVE_ORDER).onResultOf(new Function<UpdatableMod, String>() {
			@Override
			public String apply(UpdatableMod input) {
				return input.getModId();
			}
		}));
	}

	private ModSorter() { }

}
