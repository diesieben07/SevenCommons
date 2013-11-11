package de.take_weiland.mods.commons.templates;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.inventory.IInventory;

public interface SyncedContainer<T extends IInventory> extends SCContainer<T> {

	void readSyncData(DataInputStream in) throws IOException;

	void writeSyncData(DataOutputStream out) throws IOException;
	
}
