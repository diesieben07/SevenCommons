package de.take_weiland.mods.commons.internal;

import java.util.List;

public interface EntityProxy {

	public static final String GETTER = "_sc$getSyncedProperties";
	public static final String SETTER = "_sc$setSyncedProperties";

	List<SyncedEntityProperties> _sc$getSyncedProperties();
	
	void _sc$setSyncedProperties(List<SyncedEntityProperties> props);
	
}
