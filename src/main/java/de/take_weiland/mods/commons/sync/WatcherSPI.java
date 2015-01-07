package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.SerializationMethod;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>A factory for {@link de.take_weiland.mods.commons.sync.Watcher Watchers}. Register with
 * {@link de.take_weiland.mods.commons.sync.SyncSupport}.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface WatcherSPI {

	/**
	 * <p>Create a Watcher appropriate for the given property and sync method. If this factory cannot provide a Watcher
	 * for the given conditions, {@code null} must be returned.</p>
	 * @param propertyMetadata property metadata
	 * @param method the sync method
	 * @return a Watcher or null
	 */
	<T> Watcher<T> provideWatcher(PropertyMetadata<T> propertyMetadata, SerializationMethod method);

}
