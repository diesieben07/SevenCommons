package de.take_weiland.mods.commons.internal;

import java.util.List;

/**
 * @author diesieben07
 */
public interface EntityProxy {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/EntityProxy";
	public static final String GET_PROPERTIES = "_sc$getSyncedEntityProperties";
	public static final String SET_PROPERTIES = "_sc$setSyncedEntityProperties";

	List<SyncedEntityProperties> _sc$getSyncedEntityProperties();
	void _sc$setSyncedEntityProperties(List<SyncedEntityProperties> props);

}
