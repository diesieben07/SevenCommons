package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.sync.IEEPSyncCompanion;

import java.util.List;

/**
 * @author diesieben07
 */
public interface EntityProxy {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/EntityProxy";
    String GET_PROPERTIES = "_sc$getPropsCompanions";
    String SET_PROPERTIES = "_sc$setPropsCompanions";

    List<IEEPSyncCompanion> _sc$getPropsCompanions();

    void _sc$setPropsCompanions(List<IEEPSyncCompanion> props);

}
