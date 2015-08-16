package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.Scheduler;

/**
 * <p>Can be used for memory-saving purposes. if a task subclassing this is added to the scheduler, no additional memory
 * is used for this task. caveat: Task must not re-schedule itself in run()</p>
 *
 * @author diesieben07
 */
public abstract class SchedulerInternalTask {

    public SchedulerInternalTask next;

    /**
     * <p>Perform this task's action.</p>
     *
     * @return true to keep executing this task next tick
     */
    public abstract boolean run();

    public static void execute(Scheduler scheduler, SchedulerInternalTask task) {
        ((SchedulerBase) scheduler).addTask(task);
    }

}
