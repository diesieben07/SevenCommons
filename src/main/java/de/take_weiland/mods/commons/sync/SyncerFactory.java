package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.reflect.Property;

/**
 * <p>Factory for {@link Syncer} instances.</p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    <VAL> Syncer<VAL, ?, ?> getSyncer(Property<VAL> property);

}
