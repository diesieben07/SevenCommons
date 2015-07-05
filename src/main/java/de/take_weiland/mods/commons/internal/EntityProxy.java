package de.take_weiland.mods.commons.internal;

import net.minecraftforge.common.IExtendedEntityProperties;

import java.util.Map;

/**
 * @author diesieben07
 */
public interface EntityProxy {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/EntityProxy";
    String GET_IEEP_MAP = "_sc$getIEEPMap";

    Map<String, IExtendedEntityProperties> _sc$getIEEPMap();

}
