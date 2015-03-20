package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.syncx.SyncerCompanion;

/**
 * @author diesieben07
 */
public interface SyncedObjectProxy {

    String GET_COMPANION = "_sc$getCompanion";

    SyncerCompanion _sc$getCompanion();

}
