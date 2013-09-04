package de.take_weiland.mods.commons.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.AbstractIterator;

public final class CollectionUtils {

	private CollectionUtils() { }
	
	public static <T> T safeArrayAccess(T[] array, int index) {
		return arrayIndexExists(array, index) ? array[index] : null;
	}
	
	public static boolean arrayIndexExists(Object[] array, int index) {
		return index >= 0 && index < array.length;
	}
	
	public static <T> T defaultedArrayAccess(T[] array, int index, T defaultValue) {
		return arrayIndexExists(array, index) ? array[index] : defaultValue;
	}
	
	public static boolean listIndexExists(List<?> list, int index) {
		return index >= 0 && index < list.size();
	}
	
	public static <T> T safeListAccess(List<T> list, int index) {
		return listIndexExists(list, index) ? list.get(index) : null;
	}
	
	public static <T> Iterator<T> nCallsIterator(final Supplier<T> supplier, final int n) {
		return new AbstractIterator<T>() {

			private int counter = 0;
			
			@Override
			protected T computeNext() {
				return ++counter <= n ? supplier.get() : endOfData(); 
			}
			
		};
	}
	
	public static <T> Iterable<T> nCalls(final Supplier<T> supplier, final int n) {
		return new Iterable<T>() {

			@Override
			public Iterator<T> iterator() {
				return nCallsIterator(supplier, n);
			}
			
		};
	}
	
	public static <T> List<T> nullToEmpty(List<T> nullable) {
		return nullable == null ? Collections.<T>emptyList() : nullable;
	}
	
}
