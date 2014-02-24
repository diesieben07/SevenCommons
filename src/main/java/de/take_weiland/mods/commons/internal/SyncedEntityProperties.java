package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.Entity;

public interface SyncedEntityProperties {

	void _sc_sync_injectData(Entity owner, String identifier, int idx);
	
	void _sc_sync_tick();
	
	Entity _sc_sync_getEntity();
	
	int _sc_sync_getIndex();
	
	String _sc_sync_getIdentifier();
	
}
