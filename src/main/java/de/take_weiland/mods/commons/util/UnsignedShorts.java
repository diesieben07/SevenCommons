package de.take_weiland.mods.commons.util;

/**
 * Helper class for working with Unsigned shorts. Similar to guava's {@link com.google.common.primitives.UnsignedBytes}
 */
public final class UnsignedShorts {

	private UnsignedShorts() {
	}

	/**
	 * Maximum value an Unsigned Short can hold
	 */
	public static final int MAX_VALUE = 0xFFFF;

	/**
	 * Convert the given short to an Integer, treating the short as unsigned
	 *
	 * @param unsigned the unsigned short
	 * @return the value of the unsigned short
	 */
	public static int toInt(short unsigned) {
		return unsigned & 0xFFFF;
	}

	/**
	 * convert the given integer to an unsigned short
	 *
	 * @param value the integer to convert, must be <= {@link #MAX_VALUE}
	 * @return the unsigned short
	 */
	public static short checkedCast(int value) {
		if (value >> Short.SIZE != 0) {
			throw new IllegalArgumentException(String.format("Out of Range: %d", value));
		}
		return (short) value;
	}

}
