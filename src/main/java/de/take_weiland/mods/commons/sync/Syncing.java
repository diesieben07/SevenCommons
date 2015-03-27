package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.internal.sync.CompanionFactories;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class Syncing {

    public static void registerFactory(Class<?> baseClass, SyncerFactory factory) {
        CompanionFactories.newFactory(baseClass, factory);
    }

    private Syncing() { }
}
