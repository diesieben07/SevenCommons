package de.take_weiland.mods.commons.internal;

import java.util.List;

import de.take_weiland.mods.commons.sync.SyncedEntityProperties;

public interface EntityProxy {

	List<SyncedEntityProperties> _sc_sync_getSyncedProperties();
	
	void _sc_sync_setSyncedProperties(List<SyncedEntityProperties> props);
	
}
