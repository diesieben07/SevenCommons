package de.take_weiland.mods.commons.util;

public final class CommonUtils {

	private CommonUtils() { }
	
	public static final <T> T safeArrayAccess(T[] array, int index) {
		return arrayIndexExists(array, index) ? array[index] : null;
	}
	
	public static final boolean arrayIndexExists(Object[] array, int index) {
		return index >= 0 && index < array.length;
	}
	
	public static final <T> T defaultedArrayAccess(T[] array, int index, T defaultValue) {
		return arrayIndexExists(array, index) ? array[index] : defaultValue;
	}
	
}
