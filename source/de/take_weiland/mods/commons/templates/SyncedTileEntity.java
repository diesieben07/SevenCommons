package de.take_weiland.mods.commons.templates;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public interface SyncedTileEntity {

	public void writeData(ByteArrayDataOutput out);
	
	public void readData(ByteArrayDataInput in);
	
}
