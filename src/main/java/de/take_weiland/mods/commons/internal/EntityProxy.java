package de.take_weiland.mods.commons.internal;

import java.util.List;

public interface EntityProxy {

	List<SyncedEntityProperties> _sc_sync_getSyncedProperties();
	
	void _sc_sync_setSyncedProperties(List<SyncedEntityProperties> props);
	
}