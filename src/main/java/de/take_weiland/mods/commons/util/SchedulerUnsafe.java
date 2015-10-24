package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import sun.misc.Unsafe;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
@SuppressWarnings("UseOfSunClasses")
final class SchedulerUnsafe extends Scheduler {

    private static final Unsafe U;
    private static final long headOff;

    static {
        U = (Unsafe) JavaUtils.unsafe();
        try {
            headOff = U.objectFieldOffset(Scheduler.class.getDeclaredField("head"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    boolean casHead(@Nullable SchedulerInternalTask expect, @Nullable SchedulerInternalTask _new) {
        return U.compareAndSwapObject(this, headOff, expect, _new);
    }

}
