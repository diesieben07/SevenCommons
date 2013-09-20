package de.take_weiland.mods.commons.syncing;

import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public interface SyncedFieldAccessor extends Synced {
	
	int getFieldCount();
	
	void sendField(int fieldIndex, MinecraftDataOutput out);
	
	boolean needsUpdate(int fieldIndex);
	
	void receiveField(int fieldIndex, MinecraftDataInput in);
	
	void updateFields();
	
}