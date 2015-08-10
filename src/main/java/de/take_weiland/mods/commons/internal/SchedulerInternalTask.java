package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.util.Scheduler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

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

    private static final MethodHandle schedulerAddTaskMH;

    static {
        try {
            Method method = Scheduler.class.getDeclaredMethod("addTask", SchedulerInternalTask.class);
            method.setAccessible(true);
            schedulerAddTaskMH = MethodHandles.publicLookup().unreflect(method);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void execute(Scheduler scheduler, SchedulerInternalTask task) {
        try {
            schedulerAddTaskMH.invokeExact(scheduler, task);
        } catch (Throwable x) {
            throw new RuntimeException(x);
        }
    }

}
