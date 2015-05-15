package de.take_weiland.mods.commons.util;

import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.AbstractListeningExecutorService;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.FMLEventHandler;
import net.minecraft.launchwrapper.Launch;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>A thread-safe implementation of {@link com.google.common.util.concurrent.ListeningExecutorService} that uses the
 * main Minecraft thread to execute tasks.</p>
 * <p>Limited scheduling is available via the {@link #schedule(Runnable, long)} method.</p>
 * <p>This ExecutorService cannot be shut down or terminated.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Scheduler extends AbstractListeningExecutorService {

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
        return side.isClient() ? client : server;
    }

    @Override
    public void execute(Runnable task) {
        schedule(task, 0);
    }

    /**
     * <p>Execute the given task after {@code tickDelay} ticks have passed.</p>
     *
     * @param task      the task
     * @param tickDelay the delay, in ticks
     */
    public void schedule(Runnable task, long tickDelay) {
        checkArgument(tickDelay >= 0);
        synchronized (queue) {
            queue.add(new Task(task, tickDelay + 1));
        }
    }

    final List<Task> queue = new ArrayList<>();
    final List<Task> scheduledNow = new ArrayList<>();

    private void tick() {
        List<Task> scheduledNow = this.scheduledNow;
        List<Task> queue = this.queue;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (queue) {
            int idx = queue.size();
            while (--idx >= 0) {
                Task task = queue.get(idx);
                if (--task.ticks == 0) {
                    queue.remove(idx);
                    scheduledNow.add(task);
                }
            }
        }
        int idx = scheduledNow.size();
        while (--idx >= 0) {
            scheduledNow.remove(idx).r.run();
        }
    }

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            client = new Scheduler();
        } else {
            client = null;
        }
        server = new Scheduler();

        Launch.blackboard.put(FMLEventHandler.SCHEDULER_TEMP_KEY, new Runnable[]{
                server::tick,
                client == null ? null : client::tick
        });
    }

    private Scheduler() {
    }

    static final class Task {

        final Runnable r;
        long ticks;

        Task(Runnable r, long ticks) {
            this.r = r;
            this.ticks = ticks;
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
        List<Runnable> result = new ArrayList<>();
        synchronized (queue) {
            for (Task task : queue) {
                result.add(task.r);
            }
        }
        return result;
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

}
