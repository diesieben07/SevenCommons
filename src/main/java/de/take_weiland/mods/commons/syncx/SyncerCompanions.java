package de.take_weiland.mods.commons.syncx;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.StringUtils;
import sun.misc.Unsafe;

import java.lang.Object;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodHandles.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public abstract class SyncerCompanions {

    private static final SyncerCompanions INSTANCE;

    static {
        SyncerCompanions i = null;
        if (JavaUtils.hasUnsafe()) {
            try {
                i = (SyncerCompanions) Class.forName("de.take_weiland.mods.commons.syncx.SyncerCompanions$UsingUnsafe").newInstance();
                if (!i.performExtUnsafeChecks()) {
                    i = null;
                }
            } catch (ReflectiveOperationException e) {
                // ignore
            }
        }
        if (i == null) {
            System.out.println("USING BOXING!!!");
            i = new UsingBoxing();
        }
        INSTANCE = i;
    }

    public static SyncerCompanions instance() {
        return INSTANCE;
    }

    public abstract Object makeCompanion(int numFields);
    public abstract MethodHandle makeGetter(Class<?> type, int idx);
    public abstract MethodHandle makeSetter(Class<?> type, int idx);

    abstract boolean performExtUnsafeChecks();

    static final class UsingBoxing extends SyncerCompanions {

        @Override
        public Object makeCompanion(int numFields) {
            return new Object[numFields];
        }

        @Override
        public MethodHandle makeGetter(Class<?> type, int idx) {
            MethodHandle getter = arrayElementGetter(Object[].class);
            // asType does any necessary unboxing
            return insertArguments(getter, 1, idx).asType(methodType(type, Object.class));
        }

        @Override
        public MethodHandle makeSetter(Class<?> type, int idx) {
            MethodHandle setter = MethodHandles.arrayElementSetter(Object[].class);
            return insertArguments(setter, 1, idx).asType(methodType(void.class, Object.class, type));
        }

        @Override
        boolean performExtUnsafeChecks() {
            throw new AssertionError();
        }
    }

    static final class UsingUnsafe extends SyncerCompanions {

        @Override
        public Object makeCompanion(int numFields) {
            return new long[numFields];
        }

        @Override
        public MethodHandle makeGetter(Class<?> type, int idx) {
            try {
                Class<?> erased = type.isPrimitive() ? type : Object.class;
                String name = "get" + StringUtils.capitalize(JavaUtils.getNiceSimpleName(erased));
                MethodHandle getter = publicLookup().bind(JavaUtils.getUnsafe(), name, methodType(erased, Object.class, long.class));
                return insertArguments(getter, 1, arrOff(idx)).asType(methodType(type, Object.class));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public MethodHandle makeSetter(Class<?> type, int idx) {
            try {
                Class<?> erased = type.isPrimitive() ? type : Object.class;
                String name = "put" + StringUtils.capitalize(JavaUtils.getNiceSimpleName(erased));
                MethodHandle setter = publicLookup().bind(JavaUtils.getUnsafe(), name, methodType(void.class, Object.class, long.class, erased));
                return insertArguments(setter, 1, arrOff(idx)).asType(methodType(void.class, Object.class, type));
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        boolean performExtUnsafeChecks() {
            // some checks to make sure this undocumented behavior works
            // if this fails, fall back to boxing
            try {
                Object o = makeCompanion(2);
                MethodHandle get0 = makeGetter(String.class, 0);
                MethodHandle set0 = makeSetter(String.class, 0);
                MethodHandle get1 = makeGetter(String.class, 1);
                MethodHandle set1 = makeSetter(String.class, 1);

                if ((String) get0.invokeExact(o) != null) {
                    return false;
                }
                if ((String) get1.invokeExact(o) != null) {
                    return false;
                }

                set0.invokeExact(o, "hello");
                if (!((String) get0.invokeExact(o)).equals("hello")) {
                    return false;
                }
                set1.invokeExact(o, "world");
                if (!((String) get1.invokeExact(o)).equals("world")) {
                    return false;
                }
                if (!((String) get0.invokeExact(o)).equals("hello")) {
                    return false;
                }
            } catch (Throwable t) {
                return false;
            }
            return true;
        }

        private static long arrOff(int idx) {
            return Unsafe.ARRAY_LONG_BASE_OFFSET + idx * Unsafe.ARRAY_LONG_INDEX_SCALE;
        }
    }


}
