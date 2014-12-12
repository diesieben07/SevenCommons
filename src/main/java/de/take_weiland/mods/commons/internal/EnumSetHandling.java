package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.reflect.*;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.EnumSet;

/**
 * @author diesieben07
 */
public abstract class EnumSetHandling {

    private static final long NULL_ENUM_SET = 1L << 63L;

    public abstract <E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data);
    public abstract <E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long data);
    public abstract long asLong(EnumSet<?> set);


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

        @Override
        public <E extends Enum<E>> EnumSet<E> createShared(Class<E> clazz, long data) {
            return update(clazz, null, data);
        }

        @Override
        public <E extends Enum<E>> EnumSet<E> update(Class<E> clazz, EnumSet<E> set, long data) {
            if (set == null) {
                return EnumSetAcc.instance.newSmallES(clazz, JavaUtils)
            } else {
                set.clear();
            }

        }

        @Override
        public long asLong(EnumSet<?> set) {
            return 0;
        }
    }

    private interface EnumSetAcc {

        EnumSetAcc instance = SCReflection.createAccessor(EnumSetAcc.class);

        @Getter(field = "elements")
        @OverrideTarget("java.util.RegularEnumSet")
        long getData(EnumSet<?> set);

        @Setter(field = "elements")
        @OverrideTarget("java.util.RegularEnumSet")
        void setData(EnumSet<?> set, long data);

        @Construct
        @OverrideTarget("java.util.RegularEnumSet")
        <E extends Enum<E>> EnumSet<E> newSmallES(Class<E> clazz, E[] universe);

    }

    EnumSetHandling() { }

}
