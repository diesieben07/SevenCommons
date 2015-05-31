package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import de.take_weiland.mods.commons.Unsafe;
import de.take_weiland.mods.commons.internal.SCReflector;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
     *
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
     *
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
     *
     * @param array the array
     * @param index the index
     * @return the element at index {@code index} or null
     */
    public static <T> T get(T[] array, int index) {
        return index >= 0 && index < array.length ? array[index] : null;
    }

    /**
     * <p>Returns the value at the given index in the array or {@code defaultValue} if the index is out of bounds.</p>
     *
     * @param array        the array
     * @param index        the index
     * @param defaultValue the default value
     * @return the element at index {@code index} or the default value
     */
    public static <T, D extends T, R extends T> T get(R[] array, int index, D defaultValue) {
        return index >= 0 && index < array.length ? array[index] : defaultValue;
    }

    /**
     * <p>Returns the value at the given index in the List or null if the index is out of bounds.</p>
     *
     * @param list  the List
     * @param index the index
     * @return the element at index {@code index} or null
     */
    public static <T> T get(List<T> list, int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    /**
     * <p>Returns the value at the given index in the List or {@code defaultValue} if the index is out of bounds.</p>
     *
     * @param list         the List
     * @param index        the index
     * @param defaultValue the default value
     * @return the element at index {@code index} or the default value
     */
    public static <T, D extends T, V extends T> T get(List<V> list, int index, D defaultValue) {
        return index >= 0 && index < list.size() ? list.get(index) : defaultValue;
    }

    /**
     * <p>Remove all elements from the given Iterable.</p>
     *
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
     *
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
            return (T) SCReflector.instance.clone(t);
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Tried to clone a non-cloneable object " + t, e);
        }
    }

    /**
     * <p>Get an {@code Iterable} that iterates the class hierarchy of the given class in subclass to superclass order.</p>
     * <p>If {@code interfaceBehavior} is set to {@code INCLUDE}, each class in the hierarchy will be followed by the
     * interfaces implemented by that class, unless they have already been returned by the Iterator.</p>
     *
     * @param clazz             the class
     * @param interfaceBehavior whether or not to include interfaces
     * @return an Iterable
     */
    // need this because we actually only have Apache Commons 3.1, not 3.2
    public static Iterable<Class<?>> hierarchy(final Class<?> clazz, final Interfaces interfaceBehavior) {
        return () -> {
            Iterator<Class<?>> hierarchy = new HierarchyIterator(clazz);
            return interfaceBehavior == Interfaces.IGNORE ? hierarchy : new InterfaceIterator(hierarchy);
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
            ifaceFilter = seenInterfaces::add;
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
     *
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
     *
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
    }

    private JavaUtils() {
    }

}
