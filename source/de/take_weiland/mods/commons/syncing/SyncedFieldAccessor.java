package de.take_weiland.mods.commons.syncing;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public interface SyncedFieldAccessor extends Synced {
	
	int getFieldCount();
	
	void sendField(int fieldIndex, ByteArrayDataOutput out);
	
	boolean needsUpdate(int fieldIndex);
	
	void receiveField(int fieldIndex, ByteArrayDataInput in);
	
	void updateFields();
	
}