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
	 * <p>Register a ValueSyncer for the given Class T.</p>
	 * <p>The syncer class must have one of the following public constructors:</p>
	 * <ul>
	 *     <li><code>public X()</code></li>
	 *     <li><code>public X(java.lang.reflect.Member)</code> - The Field or Getter-Method will be passed</li>
	 *     <li><code>public X(java.lang.Class&lt;?&gt;)</code> - The actual declared type will be passed</li>
	 *     <li><code>public X(java.lang.reflect.Type)</code> - The actual declared generic type will be passed</li>
	 * </ul>
	 * @param clazz the class the syncer can sync
	 * @param syncer the syncer class
	 */
	public static <T> void registerSyncer(Class<T> clazz, Class<? extends ValueSyncer<T>> syncer) {
		SyncingManager.regSyncer(clazz, syncer);
	}

	/**
	 * <p>Register a ContentSyncer for the given Class T.</p>
	 * <p>The syncer class must have one of the following public constructors:</p>
	 * <ul>
	 *     <li><code>public X()</code></li>
	 *     <li><code>public X(java.lang.reflect.Member)</code> - The Field or Getter-Method will be passed</li>
	 *     <li><code>public X(java.lang.Class&lt;?&gt;)</code> - The actual declared type will be passed</li>
	 *     <li><code>public X(java.lang.reflect.Type)</code> - The actual declared generic type will be passed</li>
	 * </ul>
	 * @param clazz the class the syncer can sync
	 * @param watcher the syncer class
	 */
	public static <T> void registerContentSyncer(Class<T> clazz, Class<? extends ContentSyncer<T>> watcher) {
		SyncingManager.regContentSyncer(clazz, watcher);
	}

	private Syncing() { }
}
