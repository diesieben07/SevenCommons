package de.take_weiland.mods.commons.internal.sync;

import java.io.DataInput;
import java.io.DataOutput;

public interface SyncedObject {

	boolean _SC_SYNC_isDirty();
	
	void _SC_SYNC_write(DataOutput out);
	
	void _SC_SYNC_read(DataInput in);
	
}
