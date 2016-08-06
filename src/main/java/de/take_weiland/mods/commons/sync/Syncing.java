package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync_olds.SyncCompanions;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Registry for SyncerFactory.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Syncing {

    /**
     * <p>Register a {@link SyncerFactory}. This factory will get called back for any property that has type {@code baseClass}
     * or a subclass or implemented interface (e.g. a field of type {@code String} will query all factories registered for types
     * {@code String}, {@code Serializable}, {@code Comparable}, {@code CharSequence}, {@code Object} in that order).
     * If baseClass is Object, the factory will also receive callbacks for primitive types.</p>
     *
     * @param baseClass the baseClass
     * @param factory   the factory
     */
    public static void registerFactory(Class<?> baseClass, SyncerFactory factory) {
        SyncCompanions.registerSyncerFactory(baseClass, factory);
    }

    private Syncing() {
    }
}
