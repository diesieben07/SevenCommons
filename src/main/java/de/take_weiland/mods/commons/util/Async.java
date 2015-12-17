package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.SevenCommons;

import java.util.concurrent.ScheduledExecutorService;

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
        return SevenCommons.commonScheduler;
    }

    private Async() { }
}
