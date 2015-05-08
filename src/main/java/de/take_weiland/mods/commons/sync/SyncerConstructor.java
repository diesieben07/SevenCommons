package de.take_weiland.mods.commons.sync;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface SyncerConstructor<VAL> {

    <OBJ> Syncer<?> create(OBJ obj, PropertyAccess<OBJ, VAL> property);

}
