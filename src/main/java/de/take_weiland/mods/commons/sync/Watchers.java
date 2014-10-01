package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.SyncingManager;

/**
 * @author diesieben07
 */
public final class Watchers {

	public static <T> void registerSyncer(@org.jetbrains.annotations.NotNull Class<T> clazz, @org.jetbrains.annotations.NotNull Class<? extends PropertySyncer<T>> syncer) {
		SyncingManager.registerSyncer(clazz, syncer);
	}

	public static <T> void registerWatcher(@org.jetbrains.annotations.NotNull Class<T> clazz, @org.jetbrains.annotations.NotNull Class<? extends PropertyWatcher<? extends T>> watcher) {
		SyncingManager.registerWatcher(clazz, watcher);
	}

	private Watchers() { }
}
