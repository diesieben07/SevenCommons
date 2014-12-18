package de.take_weiland.mods.commons.syncx;

import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public interface WatcherProvider {

    @Nullable
    <T> Watcher<T> getWatcher(@Nonnull TypeToken<T> type);

}
