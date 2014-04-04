package de.take_weiland.mods.commons.meta;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
abstract class AbstractProperty<T> implements MetadataProperty<T> {

	private final int shift;
	private final int mask;

	AbstractProperty(int shift, int len) {
		checkArgument(len >= 2, "Must have at least 2 elements!");
		this.shift = shift;
		int bitCount = Integer.numberOfTrailingZeros(Integer.highestOneBit(len - 1)) + 1;
		System.out.println("bitCount: " + bitCount);
		this.mask = (1 << bitCount) - 1;
		System.out.println(this + " has mask " + Integer.toBinaryString(mask));
	}

	@Override
	public final T value(int metadata) {
		return value0((metadata >> shift) & mask);
	}

	@Override
	public final int toMeta(T value, int previousMeta) {
		return previousMeta | (toMeta0(value) << shift);
	}

	abstract T value0(int metadata);

	abstract int toMeta0(T value);
}
