package de.take_weiland.mods.commons.util;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>Utilities regarding asynchronous tasks.</p>
 *
 * @author diesieben07
 */
public final class Async {

    /**
     * <p>A common thread pool for asynchronous executions.</p>
     *
     * @return a common executor
     */
    public static ScheduledExecutorService commonExecutor() {
        return commonScheduler;
    }

    static final ScheduledExecutorService commonScheduler;

    static {
        // TODO config
        int proc = Runtime.getRuntime().availableProcessors();
        commonScheduler = Executors.newScheduledThreadPool(proc, new ThreadFactoryBuilder()
                .setNameFormat("SevenCommonsPool %s")
                .setDaemon(true)
                .build());

        MoreExecutors.addDelayedShutdownHook(Async.commonScheduler, 30, TimeUnit.SECONDS);
    }

    private Async() { }
}
