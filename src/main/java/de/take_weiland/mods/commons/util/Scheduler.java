package de.take_weiland.mods.commons.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SCModContainer;
import net.minecraft.server.ThreadMinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Scheduler implements ITickHandler, ListeningScheduledExecutorService {

	public static Scheduler server() {
		return server != null ? server : (server = new Scheduler(Side.SERVER));
	}

	public static Scheduler client() {
		return client != null ? client : (client = new Scheduler(Side.CLIENT));
	}

	public static Scheduler get(Side side) {
		return side.isClient() ? client() : server();
	}

	public static Scheduler forEnvironment() {
		return get(Sides.environment());
	}

	/**
	 * {@inheritDoc}
	 * <p>This Executor executes the Runnable on the next tick.</p>
	 *
	 * @param task the task to run
	 */
	@Override
	public void execute(@NotNull Runnable task) {
		scheduleSimple(task, 0, true);
	}

	/**
	 * Schedule the execution of the Runnable after {@code ticks} Ticks have occured.
	 *
	 * @param task    the task to run
	 * @param ticks   how many ticks to wait before execution
	 * @param tickEnd whether the task should be executed on tickEnd or not
	 */
	public void scheduleSimple(@NotNull Runnable task, int ticks, boolean tickEnd) {
		checkArgument(ticks >= 0, "Ticks must not be negative");
		long when = ticks + now.get();
		newTask(new SimpleTask(checkNotNull(task), when, tickEnd));
	}

	public void scheduleSimple(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
		scheduleSimple(task, Ints.saturatedCast(millisToTicks(unit.toMillis(delay))), true);
	}

	/**
	 * Throws the given Throwable in the main Minecraft thread, to properly trigger CrashReports.
	 *
	 * @param t the Throwable to throw
	 */
	public void throwInMainThread(@NotNull final Throwable t) {
		//noinspection ThrowableResultOfMethodCallIgnored
		checkNotNull(t);
		execute(new Runnable() {

			@Override
			public void run() {
				throw JavaUtils.throwUnchecked(t);
			}
		});
	}

	// ExecutorService methods
	@NotNull
	@Override
	public <T> ListenableFuture<T> submit(@NotNull Runnable r, @Nullable T result) {
		ScheduledRunnable<T> task = new ScheduledRunnable<>(checkNotNull(r, "Runnable"), result, now.get());
		newTask(task);
		return task;
	}

	@NotNull
	@Override
	public ScheduledListenableFuture<?> schedule(@NotNull Runnable command, long delay, @NotNull TimeUnit unit) {
		long when = millisToTicks(unit.toMillis(delay)) + now.get();
		ScheduledRunnable<Void> task = new ScheduledRunnable<>(checkNotNull(command, "Runnable"), null, when);
		newTask(task);
		return task;
	}

	@NotNull
	@Override
	public <V> ScheduledListenableFuture<V> schedule(@NotNull Callable<V> callable, long delay, @NotNull TimeUnit unit) {
		long when = millisToTicks(unit.toMillis(delay)) + now.get();
		ScheduledCallable<V> task = new ScheduledCallable<>(checkNotNull(callable, "Callable"), when);
		newTask(task);
		return task;
	}

	@NotNull
	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(@NotNull final Runnable command, long initialDelay, long delay, @NotNull TimeUnit unit) {
		long initialWhen = millisToTicks(unit.toMillis(initialDelay)) + now.get();
		long delayTicks = millisToTicks(unit.toMillis(delay));
		RepeatedRunnable task = new RepeatedRunnable(initialWhen, delayTicks, checkNotNull(command, "Runnable"));
		newTask(task);
		return task;
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		if (tasks.size() == 0) {
			return ImmutableList.of();
		}
		long when = now.get();
		boolean immediate = isOnMainThread(side);
		boolean failed = false;
		List<Future<T>> futures = Lists.newArrayListWithCapacity(tasks.size());
		for (Callable<T> callable : tasks) {
			ScheduledCallable<T> task = new ScheduledCallable<>(callable, when);
			futures.add(task);
			// if we are the main thread, execute the stuff immediately, otherwise we would wait on ourselves
			if (!immediate) {
				newTask(task);
			} else if (!failed) {
				failed = !task.run();
			}
		}

		// if we are another thread, wait for all futures to complete or throw TimeoutException
		if (!immediate) {
			long start = System.nanoTime();
			for (Future<T> future : futures) {
				if (future.isDone()) continue;

				try {
					// timeout < 0 => wait infinitely
					if (timeout < 0) {
						future.get();
					} else {
						future.get(timeout, unit);
					}
				} catch (ExecutionException | TimeoutException e) {
					failed = true;
					break;
				}
				long after = System.nanoTime();
				timeout -= (after - start);
				start = after;
			}
		}

		if (failed) {
			for (Future<T> future : futures) {
				future.cancel(true);
			}
		}
		return futures;
	}

	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks, long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		long timeoutNanos = unit.toNanos(timeout);
		if (isOnMainThread(side)) {
			Exception lastEx = null;
			long start = System.nanoTime();
			for (Callable<T> callable : tasks) {
				try {
					return callable.call();
				} catch (Exception e) {
					lastEx = e;
					// ignored, try next callable
				}
				long end = System.nanoTime();
				if (timeout >= 0 && end - start > timeoutNanos) {
					throw new TimeoutException();
				}
			}
			throw new ExecutionException(lastEx);
		}
		long start = System.nanoTime();
		Exception toThrow = null;
		for (Callable<T> task : tasks) {
			Future<T> future = submit(task);
			try {
				return timeout >= 0 ? future.get(timeoutNanos, TimeUnit.NANOSECONDS) : future.get();
			} catch (CancellationException | ExecutionException e) {
				toThrow = e;
				// ignored, try next callable
			}
			long end = System.nanoTime();
			timeoutNanos -= (end - start);
		}
		throw new ExecutionException(toThrow);
	}

	// Bouncer methods from ExecutorService

	@NotNull
	@Override
	public <T> ListenableFuture<T> submit(@NotNull Callable<T> task) {
		return schedule(task, 0, TimeUnit.MILLISECONDS);
	}

	@NotNull
	@Override
	public ListenableFuture<?> submit(@NotNull Runnable r) {
		return submit(r, null);
	}

	/**
	 * {@inheritDoc}
	 * Because this Executor is synchronous, this method acts exactly the same as {@link #scheduleWithFixedDelay(Runnable, long, long, java.util.concurrent.TimeUnit)}
	 *
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 */
	@NotNull
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
		// our tasks run synchronously, so these do the same
		return scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	@NotNull
	@Override
	public <T> List<Future<T>> invokeAll(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return invokeAll(tasks, -1, TimeUnit.NANOSECONDS);
	}

	@NotNull
	@Override
	public <T> T invokeAny(@NotNull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		try {
			return invokeAny(tasks, -1, TimeUnit.NANOSECONDS);
		} catch (TimeoutException e) {
			// impossible, TOE is only thrown if timeout >= 0
			throw new AssertionError("Impossibru!", e);
		}
	}

	// internal implementation

	void newTask(Task task) {
		synchronized (queue) {
			queue.offer(task);
		}
	}

	private static boolean isOnMainThread(Side side) {
		if (side.isServer()) {
			return Thread.currentThread() instanceof ThreadMinecraftServer;
		} else {
			return Thread.currentThread().getId() == SCModContainer.clientMainThreadID;
		}
	}

	private static Scheduler server, client;

	final AtomicLong now;
	private final Side side;
	private final EnumSet<TickType> ticks;
	private final PriorityQueue<Task> queue;
	private final ArrayList<Task> scheduledNow;
	long firstTickSystemNanos = -1;

	private Scheduler(Side side) {
		this.side = side;
		ticks = EnumSet.of(side.isServer() ? TickType.SERVER : TickType.CLIENT);
		queue = new PriorityQueue<>(10, TASK_COMPARATOR);
		now = new AtomicLong(0);
		scheduledNow = Lists.newArrayList();

		TickRegistry.registerTickHandler(this, side);
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		long now = this.now.getAndIncrement();
		if (now == 0) {
			firstTickSystemNanos = System.nanoTime();
		}
		List<Task> sn = scheduledNow;
		sn.clear();
		synchronized (queue) {
			while (queue.size() > 0) {
				Task next = queue.peek();
				if (next.when() > now) {
					break;
				}
				sn.add(queue.poll());
			}
		}
		for (int i = sn.size() - 1; i >= 0; --i) { // traverse backwards so we can fast-remove
			if (sn.get(i).tickEnd()) { // if we encounter the first tickEnd element, any further elements will be tickEnd, too, so we can stop traversing
				break;
			}
			sn.remove(i).run();
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		List<Task> sn = scheduledNow;
		for (int i = sn.size() - 1; i >= 0; --i) {
			sn.get(i).run();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return ticks;
	}

	@Override
	public String getLabel() {
		return "SevenCommonsScheduler[" + side + "]";
	}

	// unsupported methods from ExecutorService
	@Override
	public void shutdown() {
		throw unsupported();
	}

	@NotNull
	@Override
	public List<Runnable> shutdownNow() {
		throw unsupported();
	}

	@Override
	public boolean isShutdown() {
		throw unsupported();
	}

	@Override
	public boolean isTerminated() {
		throw unsupported();
	}

	@Override
	public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		throw unsupported();
	}

	private static RuntimeException unsupported() {
		throw new UnsupportedOperationException("Cannot shutdown this Scheduler!");
	}

	private interface Task {

		boolean run();

		long when();

		boolean tickEnd();

	}

	private static class SimpleTask implements Task {

		private final long when;
		private final boolean tickEnd;
		private final Runnable command;

		SimpleTask(Runnable command, long when, boolean tickEnd) {
			this.tickEnd = tickEnd;
			this.when = when;
			this.command = command;
		}

		@Override
		public boolean run() {
			command.run();
			return true;
		}

		@Override
		public long when() {
			return when;
		}

		@Override
		public boolean tickEnd() {
			return tickEnd;
		}
	}

	private abstract class TaskWithFuture<T> extends AbstractFuture<T> implements Task, ScheduledListenableFuture<T> {

		long when;

		TaskWithFuture(long when) {
			this.when = when;
		}

		public final boolean run() {
			if (isCancelled()) {
				return false;
			}
			try {
				run0();
				return true;
			} catch (Exception e) {
				setException(e);
				return false;
			}
		}

		@Override
		public long when() {
			return when;
		}

		@Override
		public boolean tickEnd() {
			return true;
		}

		abstract void run0() throws Exception;

		@Override
		public long getDelay(@NotNull TimeUnit unit) {
			long now = Scheduler.this.now.get();
			return unit.convert(ticksToMillis(when - now), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(@NotNull Delayed o) {
			return Longs.compare(this.getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
		}

	}

	private class ScheduledRunnable<T> extends TaskWithFuture<T> {

		private final Runnable r;
		private final T result;

		ScheduledRunnable(Runnable r, T result, long when) {
			super(when);
			this.r = r;
			this.result = result;
		}

		@Override
		void run0() throws Exception {
			r.run();
			set(result);
		}
	}

	private class ScheduledCallable<T> extends TaskWithFuture<T> {

		private final Callable<T> callable;

		ScheduledCallable(Callable<T> callable, long when) {
			super(when);
			this.callable = callable;
		}

		@Override
		void run0() throws Exception {
			set(callable.call());
		}
	}

	private class RepeatedRunnable extends TaskWithFuture<Void> {

		private final Runnable r;
		private final long delay;

		private RepeatedRunnable(long when, long delay, Runnable r) {
			super(when);
			this.r = r;
			this.delay = delay;
		}

		@Override
		void run0() throws Exception {
			r.run();
			when = now.get() + delay;
			Scheduler.this.newTask(this);
		}
	}

	private static final Comparator<Task> TASK_COMPARATOR = new Comparator<Task>() {
		@Override
		public int compare(Task o1, Task o2) {
			// tasks which come earlier are first, task which run on tickStart come last
			int whenRes = Longs.compare(o1.when(), o2.when());
			if (whenRes != 0) {
				return whenRes;
			} else {
				return o1.tickEnd() ? -1 : 1;
			}
		}
	};

	static long ticksToMillis(long ticks) {
		return ticks * 50L;
	}

	static long millisToTicks(long millis) {
		return millis / 50L;
	}
}
