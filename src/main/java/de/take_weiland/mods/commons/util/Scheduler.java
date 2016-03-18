package de.take_weiland.mods.commons.util;

import com.google.common.primitives.Ints;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SchedulerBase;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.internal.SevenCommons;
import gnu.trove.list.linked.TLinkedList;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>A highly efficient, thread-safe, lock-free implementation of
 * {@link com.google.common.util.concurrent.ListeningExecutorService} that uses the
 * main Minecraft thread to execute tasks.</p>
 * <p>Limited scheduling is available via the {@link #schedule(Runnable, long)} method.</p>
 * <p>This ExecutorService cannot be shut down or terminated.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Scheduler extends SchedulerBase {

    private static final Scheduler server;
    private static final Scheduler client;

    /**
     * <p>Return a Scheduler that executes tasks on the main server thread.</p>
     *
     * @return a Scheduler
     */
    public static Scheduler server() {
        return server;
    }

    /**
     * <p>The Scheduler that executes tasks on the main client thread. On a dedicated server this method will return null.</p>
     *
     * @return a Scheduler or null
     */
    public static Scheduler client() {
        return client;
    }

    /**
     * <p>Return {@link #client()} if {@code side} is {@code Side.CLIENT}, {@link #server()} otherwise.</p>
     *
     * @param side the side
     * @return a Scheduler for the side
     */
    public static Scheduler forSide(Side side) {
        return side == Side.CLIENT ? client : server;
    }

    /**
     * <p>Execute the given task after {@code tickDelay} ticks have passed.</p>
     *
     * @param r      the task
     * @param tickDelay the delay, in ticks
     */
    public void schedule(Runnable r, long tickDelay) {
        checkArgument(tickDelay >= 0);
        addTask(new WaitingTask(r, tickDelay));
    }

    @Override
    public void execute(Runnable task) {
        addTask(new WrappedRunnable(task));
    }

    public void execute(Task task) {
        addTask(new WrappedTask(task));
    }

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            client = new Scheduler();
        } else {
            client = null;
        }
        server = new Scheduler();
    }

    /**
     * helper to add a single task
     */
    @Override
    protected void addTask(SchedulerInternalTask task) {
        newTasks.addLast(task);
    }

    private final ConcurrentLinkedDeque<SchedulerInternalTask> newTasks    = new ConcurrentLinkedDeque<>();
    private final TLinkedList<SchedulerInternalTask>           activeTasks = new TLinkedList<>();

    @Override
    protected void tick() {
        {
            SchedulerInternalTask task;
            while ((task = newTasks.pollFirst()) != null) {
                activeTasks.add(task);
            }
        }
        {
            SchedulerInternalTask curr = activeTasks.getFirst();
            while (curr != null) {
                try {
                    if (!curr.execute()) {
                        activeTasks.remove(curr);
                    }
                } catch (Throwable x) {
                    SevenCommons.log.error(String.format("Exception thrown during execution of %s", curr));
                    activeTasks.remove(curr);
                }
                curr = curr.getNext();
            }
        }
    }

    public interface Task {

        /**
         * <p>Execute this task, return true to keep executing.</p>
         *
         * @return true to keep executing
         */
        boolean execute();

    }

    private static final class WaitingTask extends SchedulerInternalTask {

        private final Runnable r;
        private long ticks;

        WaitingTask(Runnable r, long ticks) {
            this.r = r;
            this.ticks = ticks;
        }

        @Override
        public boolean execute() {
            if (--ticks == 0) {
                r.run();
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String toString() {
            return String.format("Scheduled task (task=%s, remainingTicks=%s)", r, ticks);
        }
    }

    private static class WrappedTask extends SchedulerInternalTask {

        private final Task task;

        public WrappedTask(Task task) {
            this.task = task;
        }

        @Override
        public boolean execute() {
            return task.execute();
        }

        @Override
        public String toString() {
            return task.toString();
        }
    }

    private static class WrappedRunnable extends SchedulerInternalTask {
        private final Runnable task;

        public WrappedRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public boolean execute() {
            task.run();
            return false;
        }

        @Override
        public String toString() {
            return task.toString();
        }
    }

    /**
     * @return always false
     * @deprecated always false, this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public boolean isShutdown() {
        return false;
    }

    /**
     * @return always false
     * @deprecated always false, this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public boolean isTerminated() {
        return false;
    }

    /**
     * @deprecated this ExecutorService cannot be shut down
     */
    @Override
    @Deprecated
    public void shutdown() {
    }

    /**
     * @return a list of all waiting tasks
     * @deprecated this ExecutorService cannot be shut down
     */
    @Nonnull
    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    /**
     * @param timeout the timeout
     * @param unit    TimeUnit
     * @return always false
     * @throws InterruptedException
     * @deprecated this ExecutorService cannot be shut down, always returns false after sleeping for the specified
     * amount of time
     */
    @Override
    @Deprecated
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long millis = unit.toMillis(timeout);
        long milliNanos = TimeUnit.MILLISECONDS.toNanos(millis);
        int additionalNanos = Ints.saturatedCast(unit.toNanos(timeout) - milliNanos);
        Thread.sleep(millis, additionalNanos);
        return false;
    }

    private Scheduler() {
    }

}
