package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.Internal;

import java.util.List;

@Internal
public interface EntityProxy {

	List<SyncedEntityProperties> _sc_sync_getSyncedProperties();
	
	void _sc_sync_setSyncedProperties(List<SyncedEntityProperties> props);
	
}
