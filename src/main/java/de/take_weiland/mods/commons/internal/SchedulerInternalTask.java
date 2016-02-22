package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.Scheduler;
import gnu.trove.list.TLinkable;

/**
 * <p>Can be used for memory-saving purposes. if a task subclassing this is added to the scheduler, no additional memory
 * is used for this task. caveat: Task must not re-schedule itself in run()</p>
 *
 * @author diesieben07
 */
public abstract class SchedulerInternalTask implements TLinkable<SchedulerInternalTask> {

    private SchedulerInternalTask next;
    private SchedulerInternalTask prev;

    /**
     * <p>Perform this task's action.</p>
     *
     * @return true to keep executing this task next tick
     */
    public abstract boolean execute();

    public static void add(Scheduler scheduler, SchedulerInternalTask task) {
        ((SchedulerBase) scheduler).addTask(task);
    }

    @Override
    public SchedulerInternalTask getNext() {
        return next;
    }

    @Override
    public SchedulerInternalTask getPrevious() {
        return prev;
    }

    @Override
    public void setNext(SchedulerInternalTask t) {
        next = t;
    }

    @Override
    public void setPrevious(SchedulerInternalTask t) {
        prev = t;
    }
}
