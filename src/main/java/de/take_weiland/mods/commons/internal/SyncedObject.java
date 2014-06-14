package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.DataBuf;

public interface SyncedObject {

	public static final String READ = "_sc$syncRead";

	int _sc$syncRead(DataBuf in);
	
}
