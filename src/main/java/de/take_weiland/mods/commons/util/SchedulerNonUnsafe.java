package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.SchedulerInternalTask;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author diesieben07
 */
final class SchedulerNonUnsafe extends Scheduler {

    private static final AtomicReferenceFieldUpdater<Scheduler, SchedulerInternalTask> headUpdater;

    static {
        headUpdater = Scheduler.makeHeadUpdater();
    }

    @Override
    boolean casHead(@Nullable SchedulerInternalTask expect, @Nullable SchedulerInternalTask _new) {
        return headUpdater.compareAndSet(this, expect, _new);
    }
}
