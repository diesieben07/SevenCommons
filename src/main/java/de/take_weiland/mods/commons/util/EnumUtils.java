package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.Unsafe;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class EnumUtils {


    private EnumUtils() {
    }

    /**
     * <p>Get all constants defined in the given enum class. This is equivalent to {@code E.values()} except that the array
     * returned by this method is not cloned and as thus shared across the entire application. <strong>Therefor the
     * array must not be modified!</strong></p>
     *
     * @param clazz the enum class
     * @return all defined constants
     */
    @Unsafe
    public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> clazz) {
        try {
            //noinspection unchecked
            return (E[]) (Object[]) enumConstantsGetter.invokeExact(clazz);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * <p>Get the enum constant with the given ordinal value in the given enum class.</p>
     * <p>This method is equivalent to {@code E.values()[ordinal]}, but is potentially more efficient.</p>
     *
     * @param clazz   the enum class
     * @param ordinal the ordinal value
     * @return the enum constant
     */
    public static <T extends Enum<T>> T byOrdinal(Class<T> clazz, int ordinal) {
        return getEnumConstantsShared(clazz)[ordinal];
    }

    /**
     * <p>Get the type of enum values of the given EnumSet.</p>
     *
     * @param enumSet the EnumSet
     * @return the type of enum values
     */
    public static <E extends Enum<E>> Class<E> getType(EnumSet<E> enumSet) {
        try {
            //noinspection unchecked
            return (Class<E>) enumSetTypeGetter.invokeExact(enumSet);
        } catch (Throwable t) {
            throw JavaUtils.throwUnchecked(t);
        }
    }

    public static <E extends Enum<E>> long encodeAsLong(EnumSet<E> set) {
        try {
            return (long) enumSetLongEncoder.invokeExact(set);
        } catch (ClassCastException cce) {
            throw makeESTooBigExc();
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    @NotNull
    private static IllegalArgumentException makeESTooBigExc() {
        return new IllegalArgumentException("Cannot encode EnumSet of Enum type with > 64 values");
    }

    private static final MethodHandle enumConstantsGetter;
    private static final MethodHandle enumSetTypeGetter;
    private static final MethodHandle enumSetLongEncoder;

    static {
        MethodHandle ecGetter;
        try {
            Method getECShared = Class.class.getDeclaredMethod("getEnumConstantsShared");
            getECShared.setAccessible(true);
            ecGetter = publicLookup().unreflect(getECShared);
            checkState(ecGetter.type().equals(methodType(Object[].class, Class.class)));
        } catch (Throwable e) {
            try {
                ecGetter = publicLookup().bind(new ClassValue<Object>() {
                    @Override
                    protected Object computeValue(Class<?> type) {
                        return type.getEnumConstants();
                    }
                }, "get", methodType(Object.class, Class.class))
                        .asType(methodType(Object[].class, Class.class));
            } catch (NoSuchMethodException | IllegalAccessException e1) {
                throw new RuntimeException(e);
            }
        }
        enumConstantsGetter = ecGetter;
    }

    static {
        MethodHandle esTypeGetter = null;
        try {
            for (Field field : EnumSet.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Class.class) {
                    field.setAccessible(true);
                    esTypeGetter = publicLookup().unreflectGetter(field);
                    break;
                }
            }
        } catch (IllegalAccessException ignored) {
        }

        if (esTypeGetter == null) {
            throw new RuntimeException("Could not find a type field in EnumSet!");
        }
        EnumSet<ElementType> set = EnumSet.noneOf(ElementType.class);
        try {
            checkState((Class<?>) esTypeGetter.invokeExact(set) == ElementType.class);
        } catch (Throwable e) {
            throw new RuntimeException("Type field in EnumSet is not what I thought it was...", e);
        }

        enumSetTypeGetter = esTypeGetter;
    }

    static {
        MethodHandle esLongEnc = null;
        try {
            Class<?> reClass = Class.forName("java.util.RegularEnumSet");
            for (Field field : reClass.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) && field.getType() == long.class) {
                    field.setAccessible(true);
                    esLongEnc = publicLookup().unreflectGetter(field).asType(methodType(long.class, EnumSet.class));
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException ignored) {
        }

        boolean invalid;
        if (esLongEnc == null) {
            invalid = true;
        } else {
            invalid = false;
            try {
                EnumSet<ElementType> set = EnumSet.of(ElementType.FIELD, ElementType.ANNOTATION_TYPE);
                long enc = (long) esLongEnc.invokeExact(set);
                checkState(enc == (1 << ElementType.FIELD.ordinal() | 1 << ElementType.ANNOTATION_TYPE.ordinal()));
            } catch (Throwable t) {
                invalid = true;
            }
        }

        if (invalid) {
            try {
                esLongEnc = lookup().findStatic(EnumUtils.class, "encEnumSetJava", methodType(long.class, EnumSet.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        enumSetLongEncoder = esLongEnc;
    }

    @SuppressWarnings("unused")
    private static <E extends Enum<E>> long encEnumSetJava(EnumSet<E> set) {
        if (getEnumConstantsShared(getType(set)).length > 64) {
            throw makeESTooBigExc();
        }
        long res = 0;
        for (Enum<?> e : set) {
            res |= 1 << e.ordinal();
        }
        return res;
    }
}
