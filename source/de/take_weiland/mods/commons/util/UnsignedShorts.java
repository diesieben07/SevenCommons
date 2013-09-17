package de.take_weiland.mods.commons.util;

import com.google.common.base.Preconditions;

public final class UnsignedShorts {

	private UnsignedShorts() { }
	
	public static final int MAX_VALUE = 0xFFFF;
	
	public static int toInt(short unsigned) {
		return unsigned & 0xFFFF;
	}
	
	public static short checkedCast(int value) {
		Preconditions.checkArgument(value >> Short.SIZE == 0, "out of range: %s", Integer.valueOf(value));
		return (short)value;
	}
	
}
