package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import cpw.mods.fml.common.network.NetworkMod;
import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public abstract class EnumSetHandling {

    private static final long NULL_VALUE = 1L << 63L;
    private static final Unsafe unsafe = JavaUtils.getUnsafe();
    private static final long elementsFieldOff;
    private static final long typeFieldOff;

    static {
        Field found = null;
        for (Field field : EnumSet.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Class.class) {
                found = field;
                break;
            }
        }
        if (found == null) {
            throw new RuntimeException("Could not find type field in EnumSet");
        }
        typeFieldOff = unsafe.objectFieldOffset(found);
        found = null;

        Class<?> regEnumSet;
        try {
            regEnumSet = Class.forName("java.util.RegularEnumSet");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find java.util.RegularEnumSet", e);
        }
        for (Field field : regEnumSet.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == long.class) {
                found = field;
                break;
            }
        }
        if (found == null) {
            throw new RuntimeException("Could not find elements filed in RegularEnumSet");
        }
        elementsFieldOff = unsafe.objectFieldOffset(found);
    }

    public static long asLong(EnumSet<?> enumSet) {
        if (enumSet == null) {
            return NULL_VALUE;
        } else {
            return unsafe.getLong(enumSet, elementsFieldOff);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> Class<E> getEnumType(EnumSet<E> enumSet) {
        return (Class<E>) unsafe.getObject(enumSet, typeFieldOff);
    }

    public static <E extends Enum<E>> EnumSet<E> fromLong(Class<E> baseClass, long data) {
        if (data == NULL_VALUE) {
            return null;
        } else {
            EnumSet<E> enumSet = EnumSet.noneOf(baseClass);
            unsafe.putLong(enumSet, elementsFieldOff, data);
            return enumSet;
        }
    }

    public static <E extends Enum<E>> EnumSet<E> fromLong(Class<E> enumClass, EnumSet<E> enumSet, long data) {
        if (data == NULL_VALUE) {
            return null;
        } else {
            if (enumSet == null) {
                enumSet = EnumSet.noneOf(enumClass);
            }
            unsafe.putLong(enumSet, elementsFieldOff, data);
            return enumSet;
        }
    }

    static {
        EnumSetHandling instance = null;
        try {
            if (JavaUtils.hasUnsafe()) {
                instance = (EnumSetHandling) Class.forName("de.take_weiland.mods.commons.internal.EnumSetHandling$FastImpl").newInstance();
            }
        } catch (Exception ignored) { }
        if (instance == null) {
            instance = new PureJavaImpl();
        }
        INSTANCE = instance;
    }


    static final class PureJavaImpl extends EnumSetHandling {

        @Override
        public long asLong(EnumSet<?> set) {
            if (set.isEmpty()) {
                return 0L;
            } else {
                long l = 0;
                for (Enum<?> e : set) {
                    l |= 1 << e.ordinal();
                }
                return l;
            }
        }

        @Override
        public <E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data) {
            return update(clazz, null, data);
        }

        @Override
        public <E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> enumSet, long data) {
            E[] universe = JavaUtils.getEnumConstantsShared(clazz);
            if (enumSet == null) {
                enumSet = EnumSet.noneOf(clazz);
            } else {
                enumSet.clear();
            }
            while (data != 0) {
                int idx = Long.numberOfTrailingZeros(data);
                enumSet.add(universe[idx]);
                data &= ~(1 << idx);
            }
            return enumSet;
        }
    }

    static final class FastImpl extends EnumSetHandling {

        private static final Unsafe unsafe = JavaUtils.getUnsafe();
        private static final long offset;

        static {
            try {
                Class<?> regEnumSet = Class.forName("java.util.RegularEnumSet");
                Field found = null;
                for (Field field : regEnumSet.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers()) && field.getType() == long.class) {
                        found = field;
                        break;
                    }
                }
                if (found != null) {
                    offset = unsafe.objectFieldOffset(found);
                } else {
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        @Override
        public <E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data) {
            return update(clazz, null, data);
        }

        @Override
        public <E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long data) {
            if (set == null) {
                set = EnumSet.noneOf(clazz);
            }
            unsafe.putLong(set, offset, data);
            return set;
        }

        @Override
        public long asLong(EnumSet<?> set) {
            return unsafe.getLong(set, offset);
        }
    }

    EnumSetHandling() { }

}
