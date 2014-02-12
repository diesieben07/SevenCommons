package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.net.DataBuf;

@Internal
public interface SyncedObject {

	void _sc_sync_read(DataBuf in);
	
}
