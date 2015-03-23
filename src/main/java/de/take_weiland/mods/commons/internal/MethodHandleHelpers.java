package de.take_weiland.mods.commons.internal;

import com.google.common.collect.MapMaker;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class MethodHandleHelpers {

    private static ConcurrentMap<Class<?>, MethodHandle> cache;

    public static synchronized MethodHandle equal(Class<?> type) {
        checkArgument(type != void.class);
        Class<?> erased = type.isPrimitive() ? type : Object.class;

        if (cache == null) {
            cache = new MapMaker().concurrencyLevel(2).makeMap();
        }
        MethodHandle result = cache.get(erased);

        if (result == null) {
            try {
                result = lookup().findStatic(MethodHandleHelpers.class, "eq", methodType(boolean.class, erased, erased));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(); // these all exist
            }
            cache.put(erased, result);
        }
        return result.asType(methodType(boolean.class, type, type));
    }

    private static boolean eq(Object a, Object b) {
        return a == b;
    }

    private static boolean eq(boolean a, boolean b) {
        return a == b;
    }

    private static boolean eq(byte a, byte b) {
        return a == b;
    }

    private static boolean eq(char a, char b) {
        return a == b;
    }

    private static boolean eq(short a, short b) {
        return a == b;
    }

    private static boolean eq(int a, int b) {
        return a == b;
    }

    private static boolean eq(long a, long b) {
        return a == b;
    }

    private static boolean eq(float a, float b) {
        return a == b;
    }

    private static boolean eq(double a, double b) {
        return a == b;
    }
}
