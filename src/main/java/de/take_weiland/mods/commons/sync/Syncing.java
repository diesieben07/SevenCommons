package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.SyncingManager;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Registry for {@link de.take_weiland.mods.commons.sync.ValueSyncer} and {@link de.take_weiland.mods.commons.sync.ContentSyncer}.</p>
 *
 * @see de.take_weiland.mods.commons.sync.Sync
 * @see de.take_weiland.mods.commons.sync.SyncContents
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Syncing {

	/**
	 * <p>Create a new {@link de.take_weiland.mods.commons.sync.SyncingConfigurator} for configuring the syncing for class T.</p>
	 * <p>This enables the following fluid interface:<code><pre>
	 * Syncing.sync(T.class)
	 *     .annotatedWith(SomeAnnotation.class)
	 *     .when(&lt;some predicate&gt;)
	 *     .with(new SomeSyncer());
	 * </pre></code></p>
	 * @param clazz the base class
	 * @return a SyncingConfigurator
	 */
	public static <T> SyncingConfigurator<T> sync(Class<T> clazz) {
		return SyncingManager.sync(clazz);
	}

	private Syncing() { }
}
