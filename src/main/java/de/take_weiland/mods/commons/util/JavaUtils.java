package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.OverrideTarget;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.reflect.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import java.lang.reflect.Field;
import java.util.*;

public final class JavaUtils {

	private JavaUtils() {
	}

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

	public static void clear(Iterable<?> i) {
		clear(i.iterator());
	}

	public static void clear(Iterator<?> it) {
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
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
	 * Creates a new {@link com.google.common.base.Predicate} that returns true if the input is an instance
	 * of the given class and the given predicate also applies to true.
	 *
	 * @param predicate the Predicate
	 * @param <T>       the Type of the resulting predicate, for convenience, it can always handle any input
	 * @param <F>       the Class to check for first
	 * @return a new Predicate
	 */
	public static <T, F> Predicate<T> instanceOfAnd(Class<F> clazz, Predicate<? super F> predicate) {
		//cast is safe because Predicates.and does short-circuiting
		//noinspection unchecked
		return Predicates.and(Predicates.instanceOf(clazz), (Predicate<Object>) predicate);
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
		throw (T) t;
	}

	/**
	 * encode two integers into a single long value<br />
	 * The long can be decoded with {@link #decodeIntA(long)} and {@link #decodeIntB(long)} respectively
	 */
	public static long encodeInts(int a, int b) {
		return (((long) a) << 32) | (b & 0xffffffffL);
	}

	/**
	 * decode the first int encoded into the given long with {@link #encodeInts(int, int)}
	 */
	public static int decodeIntA(long l) {
		return (int) (l >> 32);
	}

	/**
	 * decode the second int encoded into the given long with {@link #encodeInts(int, int)}
	 */
	public static int decodeIntB(long l) {
		return (int) l;
	}

	@Unsafe
	public static void setBitSetData(@NotNull BitSet bitSet, long[] data) {
		setBitSetData(bitSet, data, data.length);
	}

	@Unsafe
	public static void setBitSetData(@NotNull BitSet bitSet, long[] data, int wordsInUse) {
		SCReflector r = SCReflector.instance;
		r.setWords(bitSet, data);
		r.setWordsInUse(bitSet, wordsInUse);
		r.setSizeIsSticky(bitSet, false);
	}

	private static final long ENUM_SET_NULL = 1L << 63L;

	/**
	 * <p>Encodes the given {@code EnumSet} into a single long value.</p>
	 * <p>If the EnumSet is null, the long value {@code 1L << 63L} is returned. Otherwise a long value is returned where the
	 * i-th bit is set if and only if the enum constant with ordinal value i is present in the Set.</p>
	 * <p>This method only supports Enum types with at most 63 constants. <strong>This precondition must be ensured
	 * externally. The behaviour of this method is unspecified otherwise.</strong></p>
	 * @param set the EnumSet
	 * @return a long value
	 */
	public static <E extends Enum<E>> long encodeEnumSet(@Nullable EnumSet<E> set) {
		if (set == null) {
			return ENUM_SET_NULL;
		} else if (set.isEmpty()) {
			return 0;
		} else {
			return ENUM_SET_CODEC.encode(set);
		}
	}

	/**
	 * <p>Decode the given {@code long} value into an {@code EnumSet}. The long must follow the format specified in
	 * {@link #encodeEnumSet(java.util.EnumSet)}.</p>
	 * <p>This method only supports Enum types with at most 63 constants. <strong>This precondition must be ensured
	 * externally. The behaviour of this method is unspecified otherwise.</strong></p>
	 * @param l the long value
	 * @param enumClass the enum type
	 * @return an EnumSet or null
	 */
	public static <E extends Enum<E>> EnumSet<E> decodeEnumSet(long l, Class<E> enumClass) {
		return decodeEnumSet(l, enumClass, null);
	}

	/**
	 * <p>Decode the given {@code long} value into an {@code EnumSet}. The long must follow the format specified in
	 * {@link #encodeEnumSet(java.util.EnumSet)}.</p>
	 * <p>This method only supports Enum types with at most 63 constants. <strong>This precondition must be ensured
	 * externally. The behaviour of this method is unspecified otherwise.</strong></p>
	 * @param l the long value
	 * @param enumClass the enum type
	 * @param set an EnumSet to use instead of creating a new one
	 * @return an EnumSet or null
	 */
	public static <E extends Enum<E>> EnumSet<E> decodeEnumSet(long l, Class<E> enumClass, @Nullable EnumSet<E> set) {
		if (l == ENUM_SET_NULL) {
			return null;
		} else {
			if (set == null) {
				set = EnumSet.noneOf(enumClass);
			}
			ENUM_SET_CODEC.decode(l, enumClass, set);
			return set;
		}
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

	/**
	 * checks if sun.misc.Unsafe is available on this JVM
	 *
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

	private static EnumValueGetter ENUM_GETTER;
	private static EnumSetCodec ENUM_SET_CODEC;

	static {
		try {
			Class.forName("sun.misc.SharedSecrets");
			ENUM_GETTER = (EnumValueGetter) Class.forName("de.take_weiland.mods.commons.util.JavaUtils$EnumGetterShared").newInstance();
		} catch (Exception e) {
			SevenCommons.LOGGER.info("sun.misc.SharedSecrets not found. Falling back to default EnumGetter");
			ENUM_GETTER = new EnumGetterCloned();
		}
		try {
			Class<?> regEnumSet = Class.forName("java.util.RegularEnumSet");
			regEnumSet.getDeclaredField("elements");
			ENUM_SET_CODEC = (EnumSetCodec) Class.forName("de.take_weiland.mods.commons.util.JavaUtils$EnumSetCodecFast").newInstance();
		} catch (Exception e) {
			SevenCommons.LOGGER.info("java.util.RegularEnumSet#elements not found. Encoding EnumSets manually!");
			ENUM_SET_CODEC = new EnumSetCodecNativeJava();
		}
	}

	abstract static class EnumValueGetter {

		abstract <T extends Enum<T>> T[] getEnumValues(Class<T> clazz);

	}

	static class EnumGetterCloned extends EnumValueGetter {

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return clazz.getEnumConstants();
		}

	}

	static class EnumGetterShared extends EnumValueGetter {

		private static final JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return langAcc.getEnumConstantsShared(clazz);
		}

	}

	abstract static class EnumSetCodec {

		abstract long encode(EnumSet<?> enumSet);
		abstract <E extends Enum<E>> void decode(long l, Class<E> enumType, EnumSet<E> set);

	}

	static final class EnumSetCodecNativeJava extends EnumSetCodec {

		@Override
		long encode(EnumSet<?> enumSet) {
			long l = 0;
			for (Enum<?> e : enumSet) {
				l |= e.ordinal();
			}
			return l;
		}

		@Override
		<E extends Enum<E>> void decode(long l, Class<E> enumType, EnumSet<E> set) {
			E[] universe = getEnumConstantsShared(enumType);
			set.clear();
			for (int i = 0, len = universe.length; i < len; i++) {
				if ((l & (1 << i)) != 0) {
					set.add(universe[i]);
				}
			}
		}
	}

	static final class EnumSetCodecFast extends EnumSetCodec {

		@Override
		long encode(EnumSet<?> enumSet) {
			return RegularEnumSetAcc.instance.getElements(enumSet);
		}

		@Override
		<E extends Enum<E>> void decode(long l, Class<E> enumType, EnumSet<E> set) {
			RegularEnumSetAcc.instance.setElements(set, l);
		}
	}

	static interface RegularEnumSetAcc {

		RegularEnumSetAcc instance = SCReflection.createAccessor(RegularEnumSetAcc.class);

		@Unsafe
		@Getter(field = "elements")
		@OverrideTarget("java.util.RegularEnumSet")
		long getElements(EnumSet<?> enumSet);

		@Unsafe
		@Setter(field = "elements")
		@OverrideTarget("java.util.RegularEnumSet")
		void setElements(EnumSet<?> enumSet, long elements);

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

}
