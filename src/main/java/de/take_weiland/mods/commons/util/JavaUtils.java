package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.Invoke;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.jetbrains.annotations.NotNull;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * <p>Various utility methods.</p>
 */
@ParametersAreNonnullByDefault
public final class JavaUtils {

	/**
	 * <p>Returns a String representation of the given Object. For arrays uses the appropriate {@code toString} method
	 * {@link java.util.Arrays}, otherwise uses {@link java.util.Objects#toString(Object)}.</p>
	 * @param o the Object
	 * @return a String representation
	 */
	public static String toString(@Nullable Object o) {
		if (o instanceof boolean[]) {
			return Arrays.toString((boolean[]) o);
		} else if (o instanceof byte[]) {
			return Arrays.toString((byte[]) o);
		} else if (o instanceof short[]) {
			return Arrays.toString((short[]) o);
		} else if (o instanceof char[]) {
			return Arrays.toString((char[]) o);
		} else if (o instanceof int[]) {
			return Arrays.toString((int[]) o);
		} else if (o instanceof long[]) {
			return Arrays.toString((long[]) o);
		} else if (o instanceof float[]) {
			return Arrays.toString((float[]) o);
		} else if (o instanceof double[]) {
			return Arrays.toString((double[]) o);
		} else if (o instanceof Object[]) {
			return Arrays.deepToString((Object[]) o);
		} else {
			return Objects.toString(o);
		}
	}

	public static String defaultToString(@Nullable Object o) {
		return o == null ? "null" : o.getClass().getName() + '@' + Integer.toHexString(o.hashCode());
	}

	/**
	 * <p>Returns the value at the given index in the arrayor null if the index is out of bounds.</p>
	 * @param array the array
	 * @param index the index
	 * @return the element at index {@code index} or null
	 */
	public static <T> T get(T[] array, int index) {
		return index >= 0 && index < array.length ? array[index] : null;
	}

	/**
	 * <p>Returns the value at the given index in the array or {@code defaultValue} if the index is out of bounds.</p>
	 * @param array the array
	 * @param index the index
	 * @param defaultValue the default value
	 * @return the element at index {@code index} or the default value
	 */
	public static <T, D extends T, R extends T> T get(R[] array, int index, D defaultValue) {
		return index >= 0 && index < array.length ? array[index] : defaultValue;
	}

	/**
	 * <p>Checks whether the given index exists in the given array.</p>
	 * @param array the array
	 * @param index the index
	 * @return true if the index is not out of bounds
	 */
	public static boolean indexExists(Object[] array, int index) {
		return index >= 0 && index < array.length;
	}

	/**
	 * <p>Returns the value at the given index in the List or null if the index is out of bounds.</p>
	 * @param list the List
	 * @param index the index
	 * @return the element at index {@code index} or null
	 */
	public static <T> T get(List<T> list, int index) {
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	/**
	 * <p>Returns the value at the given index in the List or {@code defaultValue} if the index is out of bounds.</p>
	 * @param list the List
	 * @param index the index
	 * @param defaultValue the default value
	 * @return the element at index {@code index} or the default value
	 */
	public static <T, D extends T, V extends T> T get(List<V> list, int index, D defaultValue) {
		return index >= 0 && index < list.size() ? list.get(index) : defaultValue;
	}

	/**
	 * <p>Checks whether the given index exists in the given List.</p>
	 * @param list the List
	 * @param index the index
	 * @return true if the index is not out of bounds
	 */
	public static boolean indexExists(List<?> list, int index) {
		return index >= 0 && index < list.size();
	}

	/**
	 * <p>Remove all elements from the given Iterable.</p>
	 * @param iterable the Iterable
	 */
	public static void clear(Iterable<?> iterable) {
		if (iterable instanceof Collection) {
			((Collection<?>) iterable).clear();
		} else {
			clear(iterable.iterator());
		}
	}

	/**
	 * <p>Remove all elements from the given Iterator.</p>
	 * @param it the Iterator
	 */
	public static void clear(Iterator<?> it) {
		while (it.hasNext()) {
			it.next();
			it.remove();
		}
	}

	/**
	 * <p>Return the given List if it is not {@code null}, an empty, immutable List otherwise.</p>
	 * @param list the List
	 * @return {@code list} itself if it is not {@code null}, {@code Collections.emptyList()} otherwise.
	 */
	public static <T> List<T> nullToEmpty(@Nullable List<T> list) {
		return list == null ? Collections.<T>emptyList() : list;
	}

	/**
	 * <p>Concatenate the two Lists. The resulting List is an unmodifiable view.</p>
	 * @param first the first List
	 * @param second the second List
	 * @return the concatenated List
	 */
	public static <T> List<T> concat(List<? extends T> first, List<? extends T> second) {
		return new ConcatList<>(first, second);
	}

	/**
	 * <p>Concatenate the two Lists, where {@code null} is considered an empty List.
	 * The resulting List is an unmodifiable view.</p>
	 * @param first the first List
	 * @param second the second List
	 * @return the concatenated List
	 */
	public static <T> List<T> concatNullable(@Nullable List<? extends T> first, @Nullable List<? extends T> second) {
		if (first == null) {
			return second == null ? Collections.<T>emptyList() : Collections.unmodifiableList(second);
		} else if (second == null) {
			return Collections.unmodifiableList(first);
		} else {
			return concat(first, second);
		}
	}

	/**
	 * <p>Concatenate the given Iterables, where {@code null} stands for an empty Iterable.</p>
	 * @param first the first Iterable
	 * @param second the second Iterable
	 * @return {@code first} and {@code second} concatenated
	 */
	public static <T> Iterable<T> concatNullable(@Nullable Iterable<T> first, @Nullable Iterable<T> second) {
		if (first == null) {
			if (second == null) {
				return Collections.emptyList();
			} else {
				return second;
			}
		} else if (second == null) {
			return first;
		} else {
			return Iterables.concat(first, second);
		}
	}

	/**
	 * <p>Creates a new {@link com.google.common.base.Predicate} that returns true if the input is an instance
	 * of the given class and the given predicate also applies to true.</p>
	 *
	 * @param clazz the Class to check for
	 * @param predicate the Predicate
	 * @return a new Predicate
	 */
	public static <T, F> Predicate<T> instanceOfAnd(final Class<F> clazz, final Predicate<? super F> predicate) {
		return new Predicate<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean apply(@Nullable T input) {
				// cast to F is safe because we check isInstance first
				return clazz.isInstance(input) && predicate.apply((F) input);
			}
		};
	}

	public static <F, T> Function<F, T> doIfElse(final Predicate<? super F> condition, final Function<? super F, ? extends T> ifTrue, final Function<? super F, ? extends T> ifFalse) {
		return new Function<F, T>() {
			@Nullable
			@Override
			public T apply(@Nullable F input) {
				return condition.apply(input) ? ifTrue.apply(input) : ifFalse.apply(input);
			}
		};
	}

	public static <T extends Cloneable> T clone(T t) {
		try {
			//noinspection unchecked
			return (T) CloneableAcc.instance.clone(t);
		} catch (CloneNotSupportedException e) {
			throw new IllegalArgumentException("Tried to clone a non-cloneable object " + t, e);
		}
	}

	/**
	 * <p>Get an {@code Iterable} that iterates the class hierarchy of the given class in subclass to superclass order.</p>
	 * <p>If {@code interfaceBehavior} is set to {@code INCLUDE}, each class in the hierarchy will be followed by the
	 * interfaces implemented by that class, unless they have already been returned by the Iterator.</p>
	 * @param clazz the class
	 * @param interfaceBehavior whether or not to include interfaces
	 * @return an Iterable
	 */
	public static Iterable<Class<?>> hierarchy(final Class<?> clazz, final Interfaces interfaceBehavior) {
		return new Iterable<Class<?>>() {
			@NotNull
			@Override
			public Iterator<Class<?>> iterator() {
				Iterator<Class<?>> hierarchy = new HierarchyIterator(clazz);
				return interfaceBehavior == Interfaces.IGNORE ? hierarchy : new InterfaceIterator(hierarchy);
			}
		};
	}

	/**
	 * <p>Behavior for {@link #hierarchy(Class, de.take_weiland.mods.commons.util.JavaUtils.Interfaces)}.</p>
	 */
	public enum Interfaces {

		/**
		 * <p>Ignore interfaces.</p>
		 */
		IGNORE,
		/**
		 * <p>Include interfaces.</p>
		 */
		INCLUDE

	}

	static final class InterfaceIterator extends AbstractIterator<Class<?>> {

		private final Iterator<Class<?>> hierarchy;
		private final Predicate<Class<?>> ifaceFilter;
		private Iterator<Class<?>> currentIfaces;

		InterfaceIterator(Iterator<Class<?>> hierarchy) {
			this.hierarchy = hierarchy;
			final Set<Class<?>> seenInterfaces = new HashSet<>();
			ifaceFilter = new Predicate<Class<?>>() {
				@Override
				public boolean apply(@Nullable Class<?> input) {
					assert input != null;
					return seenInterfaces.add(input);
				}
			};
		}

		@Override
		protected Class<?> computeNext() {
			if (currentIfaces != null && currentIfaces.hasNext()) {
				return currentIfaces.next();
			} else if (!hierarchy.hasNext()) {
				return endOfData();
			} else {
				Class<?> next = hierarchy.next();
				currentIfaces = Iterators.filter(Iterators.forArray(next.getInterfaces()), ifaceFilter);
				return next;
			}
		}
	}

	static final class HierarchyIterator extends AbstractIterator<Class<?>> {

		private Class<?> current;

		HierarchyIterator(Class<?> root) {
			this.current = root;
		}

		@Override
		protected Class<?> computeNext() {
			Class<?> next = current;
			if (next == null) {
				return endOfData();
			} else {
				current = next.getSuperclass();
				return next;
			}
		}
	}

	private static interface CloneableAcc {

		CloneableAcc instance = SCReflection.createAccessor(CloneableAcc.class);

		@Invoke(method = "clone")
		Object clone(Object t) throws CloneNotSupportedException;

	}

	/**
	 * <p>Throw the given {@code Throwable} as if it as an unchecked Exception. This method always throws, the return type
	 * is just to satisfy the compiler.</p>
	 * <p>Usage:<pre><code>throw JavaUtils.throwUnchecked(new Throwable());
	 * </code></pre></p>
	 * @param t the Throwable
	 * @return nothing, this method always throws
	 */
	@Unsafe
	public static RuntimeException throwUnchecked(Throwable t) {
		// return is never reached
		return JavaUtils.<RuntimeException>throwUnchecked0(t);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> RuntimeException throwUnchecked0(Throwable t) throws T {
		// dirty hack. the cast doesn't exist in bytecode, so it always succeeds
		throw (T) t;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> Function<T, Class<? extends T>> getClassFunction() {
		// T.getClass returns Class<T> or subclass
		return (Function) GetClassFunc.INSTANCE;
	}

	private enum GetClassFunc implements Function<Object, Class<?>> {

		INSTANCE;

		@Nullable
		@Override
		public Class<?> apply(@Nullable Object input) {
			assert input != null;
			return input.getClass();
		}

	}
	public static boolean isVisible(Class<? extends Annotation> annotationClass) {
		Retention ret = annotationClass.getAnnotation(Retention.class);
		return ret != null && ret.value() == RetentionPolicy.RUNTIME;
	}

	/**
	 * <p>Get the number of dimensions of the given class if it represents an array or 0 otherwise.</p>
	 * @param clazz the class
	 * @return the number of dimensions
	 */
	public static int getDimensions(Class<?> clazz) {
		int dims = 0;
		String name = clazz.getName();
		for (int i = 0, len = name.length(); i < len; i++) {
			if (name.charAt(i) != '[') {
				return dims;
			} else {
				dims++;
			}
		}
		throw new AssertionError("Class.getName() is empty?!");
	}

	/**
	 * <p>Get all constants defined in the given enum class. This is equivalent to {@code E.values()} except that the array
	 * returned by this method is not cloned and as thus shared across the entire application. <strong>Therefor the
	 * array must not be modified!</strong></p>
	 * @param clazz the enum class
	 * @return all defined constants
	 */
	@Unsafe
	public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> clazz) {
		return enumValuesGetter().getEnumValues(clazz);
	}

	/**
	 * <p>Get the enum constant with the given ordinal value in the given enum class.</p>
	 * <p>This method is equivalent to {@code E.values()[ordinal]}, but is potentially more efficient.</p>
	 * @param clazz the enum class
	 * @param ordinal the ordinal value
	 * @return the enum constant
	 */
	public static <T extends Enum<T>> T byOrdinal(Class<T> clazz, int ordinal) {
		return getEnumConstantsShared(clazz)[ordinal];
	}

	/**
	 * <p>Get the type of enum values of the given EnumSet.</p>
	 * @param enumSet the EnumSet
	 * @return the type of enum values
	 */
	public static <E extends Enum<E>> Class<E> getType(EnumSet<E> enumSet) {
		return enumSetTypeGetter().getEnumType(enumSet);
	}

	/**
	 * <p>Check if {@code sun.misc.Unsafe} is available on this JVM.</p>
	 *
	 * @return true if sun.misc.Unsafe was found
	 */
	@Unsafe
	public static boolean hasUnsafe() {
		initUnsafe();
		return unsafe != null;
	}

	/**
	 * <p>Return the {@code sun.misc.Unsafe} instance if it is available, {@code null} otherwise.</p>
	 * @return the {@code sun.misc.Unsafe} instance or null
	 */
	@SuppressWarnings("unchecked")
	@Unsafe
	public static <T> T getUnsafe() {
		initUnsafe();
		return (T) unsafe;
	}

	private static Object unsafe;
	private static boolean unsafeChecked;

	private static void initUnsafe() {
		if (unsafeChecked) {
			return;
		}
		unsafeChecked = true;
		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			for (Field field : unsafeClass.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				field.setAccessible(true);
				Object value = field.get(null);
				if (unsafeClass.isInstance(value)) {
					unsafe = value;
					break;
				}
			}
		} catch (Exception e) {
			// no unsafe
		}
	}

	private static EnumValuesGetter enumValuesGetter;

	private static EnumValuesGetter enumValuesGetter() {
		EnumValuesGetter e = enumValuesGetter;
		if (e != null) return e;

		try {
			Class.forName("sun.misc.SharedSecrets");
			enumValuesGetter = (EnumValuesGetter) Class.forName("de.take_weiland.mods.commons.util.JavaUtils$EnumGetterShared").newInstance();
		} catch (Throwable t) {
			SevenCommons.LOGGER.info("sun.misc.SharedSecrets not found. Falling back to default EnumGetter");
			enumValuesGetter = new EnumGetterCloned();
		}
		return enumValuesGetter;
	}

	private static EnumSetTypeGetter enumSetTypeGetter;

	private static EnumSetTypeGetter enumSetTypeGetter() {
		EnumSetTypeGetter e = enumSetTypeGetter;
		if (e != null) return e;

		Field result = null;
		for (Field field : EnumSet.class.getDeclaredFields()) {
			if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Class.class) {
				result = field;
				break;
			}
		}
		if (result == null) {
			throw new RuntimeException("Failed to find type field in EnumSet!");
		}
		if (result.getName().equals("elementType")) {
			enumSetTypeGetter = SCReflection.createAccessor(EnumSetTypeGetter.class);
		} else {
			enumSetTypeGetter = new EnumSetTypeGetterReflect(result);
		}
		return enumSetTypeGetter;
	}

	abstract static class EnumValuesGetter {

		abstract <T extends Enum<T>> T[] getEnumValues(Class<T> clazz);

	}

	final static class EnumGetterCloned extends EnumValuesGetter {

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return clazz.getEnumConstants();
		}

	}

	final static class EnumGetterShared extends EnumValuesGetter {

		private static final JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return langAcc.getEnumConstantsShared(clazz);
		}

	}

	interface EnumSetTypeGetter {

		@Getter(field = "elementType")
		<E extends Enum<E>> Class<E> getEnumType(EnumSet<E> enumSet);

	}


	final static class EnumSetTypeGetterReflect implements EnumSetTypeGetter {

		private final Field field;

		EnumSetTypeGetterReflect(Field field) {
			this.field = field;
			field.setAccessible(true);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final <E extends Enum<E>> Class<E> getEnumType(EnumSet<E> set) {
			try {
				return (Class<E>) field.get(set);
			} catch (IllegalAccessException e) {
				throw Throwables.propagate(e);
			}
		}
	}



	private JavaUtils() { }

}
