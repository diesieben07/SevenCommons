package de.take_weiland.mods.commons.util;

import com.google.common.primitives.Ints;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SchedulerBase;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
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
        addTask(new SchedulerInternalTask() {
            @Override
            public boolean run() {
                task.run();
                return false;
            }
        });
    }

    public void execute(Task task) {
        addTask(new SchedulerInternalTask() {
            @Override
            public boolean run() {
                return task.run();
            }
        });
    }

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            client = new Scheduler();
        } else {
            client = null;
        }
        server = new Scheduler();
    }

    @SuppressWarnings("unused") // we use it, just via CAS
    private volatile SchedulerInternalTask head;

    private static final Unsafe U;
    private static final long headOff;

    static {
        U = JavaUtils.unsafe();
        try {
            headOff = U.objectFieldOffset(Scheduler.class.getDeclaredField("head"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean casHead(@Nullable SchedulerInternalTask expect, @Nullable SchedulerInternalTask _new) {
        return U.compareAndSwapObject(this, headOff, expect, _new);
    }

    /**
     * helper to add a single task
     * Do not rename, see {@link de.take_weiland.mods.commons.internal.SchedulerInternalTask}
     */
    @Override
    protected void addTask(SchedulerInternalTask task) {
        addTasks(task, task);
    }

    /**
     * add a prebuilt linked list of tasks, newHead must eventually through a chain of .next pointers point to newTail.
     * newTail.next must be meaningless, it will be overwritten
     * <p>if newHead == newTail a single task will be added
     */
    private void addTasks(SchedulerInternalTask newHead, SchedulerInternalTask newTail) {
        SchedulerInternalTask curr;
        do {
            curr = head;
            newTail.next = curr;
            // volatile write, any writes before this happen before a read on "this.head"
            // hence InternalTask.next doesn't need to be volatile
        } while (!casHead(curr, newHead));
    }

    @SuppressWarnings("unused") // see FMLEventHandler
    private void tick() {
        SchedulerInternalTask curr;
        do {
            curr = head;
            if (curr == null) {
                // no tasks
                return;
            }
            // set to null to mark tasks as done
        } while (!casHead(curr, null));

        SchedulerInternalTask rescheduleHead = null; // built list of things to reschedule here, to avoid CASing this.next for every single one of them
        SchedulerInternalTask rescheduleTail = null; // instead we CAS just once, once we are done here

        while (true) {
            SchedulerInternalTask next = curr.next;

            if (curr.run()) {
                if (rescheduleHead == null) {
                    rescheduleHead = rescheduleTail = curr;
                } else {
                    curr.next = rescheduleHead;
                    rescheduleHead = curr;
                }
            }
            if ((curr = next) == null) {
                break;
            }
        }
        if (rescheduleHead != null) {
            addTasks(rescheduleHead, rescheduleTail);
        }
    }

    public interface Task {

        /**
         * <p>Execute this task, return true to keep executing.</p>
         *
         * @return true to keep executing
         */
        boolean run();

    }

    private static final class WaitingTask extends SchedulerInternalTask {

        private final Runnable r;
        private long ticks;

        WaitingTask(Runnable r, long ticks) {
            this.r = r;
            this.ticks = ticks;
        }

        @Override
        public boolean run() {
            if (--ticks == 0) {
                r.run();
                return false;
            } else {
                return true;
            }
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
