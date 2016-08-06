package de.take_weiland.mods.commons.internal.sync_olds;

/**
 * <p>Implemented via ASM on any object that could potentially be synced (currently TileEntity, Entity, Container, IEEP).</p>
 *
 * @author diesieben07
 */
public interface SyncedObjectProxy {

    String GET_COMPANION = "_sc$getCompanion";
    String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync_olds/SyncedObjectProxy";

    /**
     * <p>Get the SyncCompanion for this object, might be null if this object is not synced.</p>
     *
     * @return the SyncCompanion
     */
    SyncCompanion _sc$getCompanion();

}
