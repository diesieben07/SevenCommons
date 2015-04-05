package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.reflect.Invoke;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.jetbrains.annotations.NotNull;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.lang.invoke.MethodHandles.publicLookup;

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

	/**
	 * <p>Return a proper hashCode for the given object. If the object is an array, the appropriate hashCode method in
	 * the {@link Arrays} class will be called.</p>
	 * @param o the object
	 * @return a hash code for the object
	 */
	public static int hashCode(Object o) {
		if (o instanceof boolean[]) {
			return Arrays.hashCode((boolean[]) o);
		} else if (o instanceof byte[]) {
			return Arrays.hashCode((byte[]) o);
		} else if (o instanceof short[]) {
			return Arrays.hashCode((short[]) o);
		} else if (o instanceof char[]) {
			return Arrays.hashCode((char[]) o);
		} else if (o instanceof int[]) {
			return Arrays.hashCode((int[]) o);
		} else if (o instanceof long[]) {
			return Arrays.hashCode((long[]) o);
		} else if (o instanceof float[]) {
			return Arrays.hashCode((float[]) o);
		} else if (o instanceof double[]) {
			return Arrays.hashCode((double[]) o);
		} else if (o instanceof Object[]) {
			return Arrays.deepHashCode((Object[]) o);
		} else {
			return Objects.hashCode(o);
		}
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

	public static <T extends Cloneable> T clone(T t) {
		try {
			//noinspection unchecked
			return (T) CloneableAcc.instance.clone(t);
		} catch (CloneNotSupportedException e) {
			throw new IllegalArgumentException("Tried to clone a non-cloneable object " + t, e);
		}
	}

	private interface CloneableAcc {

		CloneableAcc instance = SCReflection.createAccessor(CloneableAcc.class);

		@Invoke(method = "clone")
		Object clone(Object t) throws CloneNotSupportedException;

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

	/**
	 * <p>Get all constants defined in the given enum class. This is equivalent to {@code E.values()} except that the array
	 * returned by this method is not cloned and as thus shared across the entire application. <strong>Therefor the
	 * array must not be modified!</strong></p>
	 * @param clazz the enum class
	 * @return all defined constants
	 */
	@Unsafe
	public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> clazz) {
		return EnumValuesGetter.instance.getEnumValues(clazz);
	}

	abstract static class EnumValuesGetter {

		static final EnumValuesGetter instance;

		static {
			EnumValuesGetter e;
			try {
				Class.forName("sun.misc.SharedSecrets");
				e = (EnumValuesGetter) Class.forName("de.take_weiland.mods.commons.util.JavaUtils$EnumGetterShared").newInstance();
			} catch (Throwable t) {
				SevenCommons.LOGGER.info("sun.misc.SharedSecrets not found. Falling back to default EnumGetter");
				e = new EnumGetterCloned();
			}
			instance = e;
		}

		abstract <T extends Enum<T>> T[] getEnumValues(Class<T> clazz);

	}

	final static class EnumGetterCloned extends EnumValuesGetter {

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return clazz.getEnumConstants();
		}

	}

	@SuppressWarnings("unused")
	final static class EnumGetterShared extends EnumValuesGetter {

		private static final JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();

		@Override
		<T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
			return langAcc.getEnumConstantsShared(clazz);
		}

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
		try {
			//noinspection unchecked
			return (Class<E>) EnumSetAcc.getter.invokeExact(enumSet);
		} catch (Throwable t) {
			throw throwUnchecked(t);
		}
	}

	@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
	static final class EnumSetAcc {

		static final MethodHandle getter;

		static {
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
			result.setAccessible(true);
			try {
				getter = publicLookup().unreflectGetter(result);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

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

	private static volatile boolean unsafeChecked;

	private static void initUnsafe() {
		if (unsafeChecked) {
			return;
		}
		synchronized (JavaUtils.class) {
			if (unsafeChecked) return;

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
	}

	private JavaUtils() { }

}
