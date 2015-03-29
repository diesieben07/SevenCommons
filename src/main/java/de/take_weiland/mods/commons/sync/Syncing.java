package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.CompanionFactories;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Registry for SyncerFactory.</p>
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Syncing {

    /**
     * <p>Register a {@link SyncerFactory}. This factory will get called back for any property that has type {@code baseClass}
     * or a subclass or subinterface. If baseClass is Object, the factory will also receive callbacks for primitive types.</p>
     * @param baseClass the baseClass
     * @param factory the factory
     */
    public static void registerFactory(Class<?> baseClass, SyncerFactory factory) {
        CompanionFactories.newFactory(baseClass, factory);
    }

    private Syncing() { }
}
