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
		System.out.println("bitCount: " + bitCount);
		this.mask = (1 << bitCount) - 1;
		System.out.println(this + " has mask " + Integer.toBinaryString(mask));
		this.values = values;
	}

	T value0(int metadata) {
		return JavaUtils.get(values, metadata);
	}

	final T[] values() {
		return values;
	}

	@Override
	public final T value(int metadata) {
		return value0((metadata >> shift) & mask);
	}

	@Override
	public final int toMeta(T value, int previousMeta) {
		return previousMeta | (toMeta0(value) << shift);
	}

	abstract int toMeta0(T value);
}
