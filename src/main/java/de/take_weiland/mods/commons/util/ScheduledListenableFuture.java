package de.take_weiland.mods.commons.util;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ScheduledFuture;

/**
 * A {@link java.util.concurrent.ScheduledFuture} which also accepts listeners
 *
 * @author diesieben07
 */
public interface ScheduledListenableFuture<T> extends ListenableFuture<T>, ScheduledFuture<T> {
}
