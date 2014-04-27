package de.take_weiland.mods.commons.util;

import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.internal.SevenCommons;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class JavaUtils {

	private JavaUtils() { }

	/**
	 * returns the value at the given slot in the array, or null if the slot is out of bounds
	 */
	public static <T> T get(T[] array, int index) {
		return indexExists(array, index) ? array[index] : null;
	}

	/**
	 * returns true if the given slot exists in the array
	 */
	public static boolean indexExists(Object[] array, int index) {
		return index >= 0 && index < array.length;
	}

	/**
	 * returns the value at the given slot in the array, or the defaultValue if the slot is out of bounds
	 */
	public static <T> T get(T[] array, int index, T defaultValue) {
		return indexExists(array, index) ? array[index] : defaultValue;
	}

	/**
	 * returns the value at the given slot in the list, or null if the slot is out of bounds
	 */
	public static <T> T get(List<T> list, int index) {
		return indexExists(list, index) ? list.get(index) : null;
	}

	/**
	 * returns true if the given slot exists in the list
	 */
	public static boolean indexExists(List<?> list, int index) {
		return index >= 0 && index < list.size();
	}

	/**
	 * returns the value at the given slot in the list, or the defaultValue if the slot is out of bounds
	 */
	public static <T> T get(List<T> list, int index, T defaultValue) {
		return indexExists(list, index) ? list.get(index) : defaultValue;
	}

	@Deprecated
	public static <T> T safeArrayAccess(T[] array, int index) {
		return get(array, index);
	}

	@Deprecated
	public static boolean arrayIndexExists(Object[] array, int index) {
		return indexExists(array, index);
	}

	@Deprecated
	public static <T> T defaultedArrayAccess(T[] array, int index, T defaultValue) {
		return get(array, index, defaultValue);
	}

	@Deprecated
	public static boolean listIndexExists(List<?> list, int index) {
		return indexExists(list, index);
	}

	@Deprecated
	public static <T> T safeListAccess(List<T> list, int index) {
		return listIndexExists(list, index) ? list.get(index) : null;
	}

	public static <T> Iterator<T> reverse(final ListIterator<T> it) {
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return it.hasPrevious();
			}

			@Override
			public T next() {
				return it.previous();
			}

			@Override
			public void remove() {
				it.remove();
			}

		};
	}

	/**
	 * returns the given list or {@link java.util.Collections#emptyList()} if the list is null
	 */
	public static <T> List<T> nullToEmpty(List<T> nullable) {
		return nullable == null ? Collections.<T>emptyList() : nullable;
	}

	/**
	 * concatenate the given iterables, null will be treated as an empty iterable
	 */
	public static <T> Iterable<T> concatNullable(Iterable<T> a, Iterable<T> b) {
		return a == null ? (b == null ? Collections.<T>emptyList() : b) : (b == null ? a : Iterables.concat(a, b));
	}

	/**
	 * Throws the given Throwable as if it was an unchecked exception<br />
	 * This method always throws, the return type is just there to allow constructs like this:<br />
	 * {@code
	 * throw JavaUtils.throwUnchecked(new Throwable());
	 * }<br />
	 * in case a return type is expected
	 */
	@Unsafe
	public static RuntimeException throwUnchecked(Throwable t) {
		// return is never reached
		return JavaUtils.<RuntimeException>throwUnchecked0(t);
	}
	
	@SuppressWarnings("unchecked") // dirty hack. the cast doesn't exist in bytecode, so it always succeeds
	private static <T extends Throwable> RuntimeException throwUnchecked0(Throwable t) throws T {
		throw (T)t;
	}

	/**
	 * encode two integers into a single long value<br />
	 * The long can be decoded with {@link #decodeIntA(long)} and {@link #decodeIntB(long)} respectively
	 */
	public static long encodeInts(int a, int b) {
		return (((long)a) << 32) | (b & 0xffffffffL);
	}

	/**
	 * decode the first int encoded into the given long with {@link #encodeInts(int, int)}
	 */
	public static int decodeIntA(long l) {
		return (int)(l >> 32);
	}

	/**
	 * decode the second int encoded into the given long with {@link #encodeInts(int, int)}
	 */
	public static int decodeIntB(long l) {
		return (int)l;
	}
	
	@Deprecated
	@Unsafe
	public static <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
		return getEnumConstantsShared(clazz);
	}

	/**
	 * Gets all values defined in the given Enum class. The returned array is shared across the entire codebase and should never be modified!
	 */
	@Unsafe
	public static <T extends Enum<T>> T[] getEnumConstantsShared(Class<T> clazz) {
		return ENUM_GETTER.getEnumValues(clazz);
	}

	/**
	 * gets the Enum constant with the given ordinal for the given Enum class<br />
	 * This method is preferable to<br />
	 * {@code
	 * SomeEnum v = SomeEnum.values()[ordinal];
	 * }<br />
	 * because it doesn't need to copy the value array, like values() does
	 */
	public static <T extends Enum<T>> T byOrdinal(Class<T> clazz, int ordinal) {
		return get(getEnumConstantsShared(clazz), ordinal);
	}
	
	private static EnumValueGetter ENUM_GETTER;
	
	static {
		try {
			Class.forName("sun.misc.SharedSecrets");
			ENUM_GETTER = Class.forName("de.take_weiland.mods.commons.util.JavaUtils$EnumGetterShared").asSubclass(EnumValueGetter.class).newInstance();
		} catch (Exception e) {
			SevenCommons.LOGGER.info("sun.misc.SharedSecrets not found. Falling back to default EnumGetter");
			ENUM_GETTER = new EnumGetterCloned();
		}
	}

	interface EnumValueGetter {

		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz);

	}

	static class EnumGetterCloned implements EnumValueGetter {

		@Override
		public <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return clazz.getEnumConstants();
		}

	}

	static class EnumGetterShared implements EnumValueGetter {

		private JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();

		@Override
		public <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return langAcc.getEnumConstantsShared(clazz);
		}

	}
	
	private static Object unsafe;
	private static boolean unsafeChecked;
	
	private static void initUnsafe() {
		if (unsafeChecked) {
			return;
		}
		unsafeChecked = true;
		try {
			Field field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = field.get(null);
		} catch (Exception e) {
			// no unsafe
		}
	}

	/**
	 * checks if sun.misc.Unsafe is available on this JVM
	 * @return true if sun.misc.Unsafe was found
	 */
	@Unsafe
	public static boolean hasUnsafe() {
		initUnsafe();
		return unsafe != null;
	}

	/**
	 * returns the sun.misc.Unsafe instance, if available, otherwise null
	 */
	@Unsafe
	public static Object getUnsafe() {
		initUnsafe();
		return unsafe;
	}

}
