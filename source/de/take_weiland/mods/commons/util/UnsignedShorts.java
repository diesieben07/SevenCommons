package de.take_weiland.mods.commons.util;

public final class UnsignedShorts {

	private UnsignedShorts() { }
	
	public static final int toInt(short unsigned) {
		return unsigned & 0xFFFF;
	}
	
	public static final short checkedCast(int value) {
		if (value >> Short.SIZE != 0) {
			throw new IllegalArgumentException("Unsigned Short out of range: " + value);
		}
		return (short)value;
	}
	
}
