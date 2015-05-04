package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.Property;

/**
 * <p>Factory for {@link Syncer} instances.</p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    /**
     * <p>Get a {@link Syncer} that handles the given TypeSpecification.</p>
     * <p>If this factory cannot provide a Syncer for the given Property, null must be returned.</p>
     * @param type the type
     * @return a Syncer or null
     */
    <V> Syncer<V, ?, ?> getSyncer(Property<V, ?> type);

}
