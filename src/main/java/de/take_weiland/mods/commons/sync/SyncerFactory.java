package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.serialize.TypeSpecification;

/**
 * <p>A factory for a handlers that allow some type T to be used with the {@link de.take_weiland.mods.commons.sync.Sync @Sync}
 * annotation.</p>
 *
 * @author diesieben07
 */
public interface SyncerFactory {

    <V, C> Syncer<V, C> getSyncer(TypeSpecification<V> type);

}
