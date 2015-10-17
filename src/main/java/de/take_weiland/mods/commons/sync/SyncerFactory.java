package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.reflect.Property;

/**
 * <p>Factory for {@link Syncer} instances.</p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    /**
     * <p>Get a {@code Syncer} for the given property.</p>
     * <p>If this factory does not support the given property, null must be returned.</p>
     * @param property the property to sync
     * @return a {@code Syncer} or null
     */
    <VAL> Syncer<VAL, ?, ?> getSyncer(Property<VAL> property);

}
