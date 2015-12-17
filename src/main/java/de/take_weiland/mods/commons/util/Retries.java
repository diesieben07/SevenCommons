package de.take_weiland.mods.commons.util;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * <p>Utilities for retrying tasks.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Retries {

    /**
     * <p>Retry the given task with the given settings synchronously in the current thread.</p>
     *
     * @param callable the task to perform
     * @param settings the settings
     * @return the result of the task
     * @throws TooManyRetries       when the task failed too many times as specified by the settings
     * @throws CompletionException  when the task fails with an exception unspecified by the settings
     * @throws InterruptedException when the thread is interrupted
     */
    public static <T> T retry(Callable<? extends T> callable, RetrySettings settings) throws InterruptedException {
        int run = 0;
        while (true) {
            try {
                return callable.call();
            } catch (Throwable x) {
                if (settings.test(x)) {
                    int delay = settings.delay(run++);
                    if (delay == RetrySettings.GIVE_UP) {
                        throw new TooManyRetries("Tasked failed too many times", x);
                    } else {
                        Thread.sleep(delay);
                    }
                } else {
                    throw new CompletionException("Task failed with non-matching exception", x);
                }
            }
        }
    }

    /**
     * <p>Create a CompletionStage that will complete successfully when the callable eventually completes when retrying it as specified by the settings.</p>
     * <p>If the callable fails with an exception which is not specified by the settings, the CompletionStage will immediately fail with that exception.
     * If the callable does not succeed on the final try as specified by the characteristics, the CompletionStage will immediately fail with the exception that caused
     * this final try to fail.</p>
     *
     * @param callable the callable
     * @param settings the settings
     * @return a CompletionStage
     */
    public static <T> CompletionStage<T> retryAsync(Callable<? extends T> callable, RetrySettings settings) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Async.commonExecutor().execute(new TryingRunner<>(callable, future, settings));
        return future;
    }

    /**
     * <p>Create a {@code RetrySettings} object which will retry for all exceptions with the specified delays
     * (in milliseconds) and then give up.</p>
     *
     * @param delays the delays
     * @return RetrySettings
     */
    public static RetrySettings settings(int... delays) {
        return new SettingsImpl(delays);
    }

    /**
     * <p>Create a {@code RetrySettings} object which will retry for exceptions of the given type with the specified delays
     * (in milliseconds) and then give up.</p>
     *
     * @param exClass the exception class
     * @param delays  the delays
     * @return RetrySettings
     */
    public static RetrySettings settings(Class<? extends Throwable> exClass, int... delays) {
        return new SettingsImpl(delays) {
            @Override
            public boolean test(Throwable x) {
                return exClass.isInstance(x);
            }
        };
    }

    private static class SettingsImpl implements RetrySettings {

        private final int[] delays;

        SettingsImpl(int[] delays) {
            this.delays = delays;
        }

        @Override
        public int delay(int tryNo) {
            if (tryNo < delays.length) {
                return delays[tryNo];
            } else {
                return GIVE_UP;
            }
        }

        @Override
        public boolean test(Throwable x) {
            return true;
        }
    }

    /**
     * <p>Specification about how a task should be retried. Implementations of this interface must be threadsafe.</p>
     */
    public interface RetrySettings extends Predicate<Throwable> {

        /**
         * <p>Use this return value in {@link #delay(int)} to specify no further retries.</p>
         */
        int GIVE_UP = -1;

        /**
         * <p>The number of milliseconds to wait before the next try or {@link #GIVE_UP} to specify no further retries.</p>
         *
         * @param tryNo the number of the try that just failed, starting at 0
         * @return the number of milliseconds to wait
         */
        int delay(int tryNo);

        /**
         * <p>Whether the given {@code Throwable} should trigger a retry. If false is returned here no further retries are
         * to be performed and the execution must fail.</p>
         *
         * @param x the {@code Throwable} to check
         * @return true to trigger a retry
         */
        @Override
        boolean test(Throwable x);
    }

    public static final class TooManyRetries extends CompletionException {

        TooManyRetries(String msg, Throwable cause) {
            super(msg, cause);
        }

    }

    private static final class TryingRunner<T> implements Runnable {
        private final Callable<? extends T> callable;
        private final CompletableFuture<T>  future;
        private final RetrySettings         settings;

        private int n = 0;

        private TryingRunner(Callable<? extends T> callable, CompletableFuture<T> future, RetrySettings settings) {
            this.callable = callable;
            this.future = future;
            this.settings = settings;
        }

        @Override
        public void run() {
            try {
                future.complete(callable.call());
            } catch (Throwable x) {
                if (settings.test(x)) {
                    int delay = settings.delay(n++);
                    if (delay == RetrySettings.GIVE_UP) {
                        future.completeExceptionally(x);
                    } else {
                        Async.commonExecutor().schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                } else {
                    future.completeExceptionally(x);
                }
            }
        }

    }

    private Retries() {
    }

}
