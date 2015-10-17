package de.take_weiland.mods.commons.internal;

import com.google.common.util.concurrent.AbstractListeningExecutorService;

/**
 * @author diesieben07
 */
public abstract class SchedulerBase extends AbstractListeningExecutorService {

    protected abstract void addTask(SchedulerInternalTask task);

    protected abstract void tick();

}
