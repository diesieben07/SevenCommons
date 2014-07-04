package de.take_weiland.mods.commons.meta;

import de.take_weiland.mods.commons.util.JavaUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
abstract class AbstractArrayProperty<T> extends GenericProperty<T> {

	private final T[] values;
	private final int shift;
	private final int mask;

	AbstractArrayProperty(int shift, T[] values) {
		checkArgument(values.length >= 2, "Must have at least 2 elements!");
		this.shift = shift;
		int bitCount = Integer.numberOfTrailingZeros(Integer.highestOneBit(values.length - 1)) + 1;
		checkArgument(shift >= bitCount + 1, "Too many values for given shift (need at least %s)", bitCount - 1);
		this.mask = (1 << bitCount) - 1;
		this.values = values;
	}

	@Override
	public final T[] values() {
		return values;
	}

	@Override
	public final boolean hasDistinctValues() {
		return true;
	}

	@Override
	public final T value(int metadata) {
		return JavaUtils.get(values, (metadata >> shift) & mask);
	}

	@Override
	public final int toMeta(T value, int previousMeta) {
		return previousMeta | (toMeta0(value) << shift);
	}

	abstract int toMeta0(T value);
}
