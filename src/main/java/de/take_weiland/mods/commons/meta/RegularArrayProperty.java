package de.take_weiland.mods.commons.meta;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
final class RegularArrayProperty<T> extends AbstractArrayProperty<T> {

	private final Map<T, Integer> lookup;

	RegularArrayProperty(int shift, T[] values) {
		super(shift, values);
		ImmutableMap.Builder<T, Integer> b = ImmutableMap.builder();
		for (int i = 0; i < values.length; ++i) {
			b.put(checkNotNull(values[i], "Cannot have null properties!"), i);
		}
		lookup = b.build();
	}

	@Override
	int toMeta0(T value) {
		Integer i = lookup.get(checkNotNull(value, "value"));
		checkArgument(i != null, "Unknown property %s", value);
		return i;
	}
}
