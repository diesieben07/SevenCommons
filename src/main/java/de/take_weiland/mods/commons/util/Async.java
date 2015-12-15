package de.take_weiland.mods.commons.util;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.*;
import java.util.function.Predicate;

/**
 * <p>Utilities regarding asynchronous tasks.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Async {

    private static final ScheduledExecutorService commonScheduler;

    static {
        // TODO config
        int proc = Runtime.getRuntime().availableProcessors();
        commonScheduler = Executors.newScheduledThreadPool(proc, new ThreadFactoryBuilder()
                .setNameFormat("SevenCommonsPool %s")
                .setDaemon(true)
                .build());

        MoreExecutors.addDelayedShutdownHook(commonScheduler, 30, TimeUnit.SECONDS);
    }

    public static ScheduledExecutorService commonExecutor() {
        return commonScheduler;
    }

    /**
     * <p>Create a CompletionStage that will complete successfully when the callable eventually completes when retrying it as specified by the characteristics.</p>
     * <p>If the callable fails with an exception which is not specified by the characteristics, the CompletionStage will immediately fail with that exception.
     * If the callable does not succeed on the final try as specified by the characteristics, the CompletionStage will immediately fail with the exception that caused
     * this final try to fail.</p>
     *
     * @param callable        the callable
     * @param characteristics the characteristics
     * @return a CompletionStage
     */
    public static <T> CompletionStage<T> retrying(Callable<? extends T> callable, RetryCharacteristics characteristics) {
        CompletableFuture<T> future = new CompletableFuture<>();
        commonExecutor().execute(new TryingRunner<>(callable, future, characteristics));
        return future;
    }

    /**
     * <p>Create a {@code RetryCharacteristics} object which will retry for exceptions of the given type with the specified delays and then give up.</p>
     *
     * @param exClass the exception class
     * @param delays  the delays
     * @return RetryCharacteristics
     */
    public static RetryCharacteristics characteristics(Class<? extends Throwable> exClass, int... delays) {
        return new RetryCharacteristics() {
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
                return exClass.isInstance(x);
            }
        };
    }

    private static final class TryingRunner<T> implements Runnable {

        private final Callable<? extends T> callable;
        private final CompletableFuture<T>  future;
        private final RetryCharacteristics  characteristics;
        private int n = 0;

        private TryingRunner(Callable<? extends T> callable, CompletableFuture<T> future, RetryCharacteristics characteristics) {
            this.callable = callable;
            this.future = future;
            this.characteristics = characteristics;
        }

        @Override
        public void run() {
            try {
                future.complete(callable.call());
            } catch (Throwable x) {
                if (characteristics.test(x)) {
                    int delay = characteristics.delay(n++);
                    if (delay == RetryCharacteristics.GIVE_UP) {
                        future.completeExceptionally(x);
                    } else {
                        commonExecutor().schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                } else {
                    future.completeExceptionally(x);
                }
            }
        }

    }

    public interface RetryCharacteristics extends Predicate<Throwable> {

        int GIVE_UP = -1;

        int delay(int tryNo);

    }

    private Async() {
    }

}
