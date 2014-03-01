package de.take_weiland.mods.commons.util;

import com.google.common.base.Preconditions;

/**
 * Helper class for working with Unsigned shorts. Similar to guava's {@link com.google.common.primitives.UnsignedBytes}
 */
public final class UnsignedShorts {

	private UnsignedShorts() { }

	/**
	 * Maximum value an Unsigned Short can hold
	 */
	public static final int MAX_VALUE = 0xFFFF;

	/**
	 * Convert the given short to an Integer, treating the short as unsigned
	 * @param unsigned the unsigned short
	 * @return the value of the unsigned short
	 */
	public static int toInt(short unsigned) {
		return unsigned & 0xFFFF;
	}

	/**
	 * convert the given integer to an unsigned short
	 * @param value the integer to convert, must be <= {@link #MAX_VALUE}
	 * @return the unsigned short
	 */
	public static short checkedCast(int value) {
		Preconditions.checkArgument(value >> Short.SIZE == 0, "out of range: %s", Integer.valueOf(value));
		return (short)value;
	}
	
}
