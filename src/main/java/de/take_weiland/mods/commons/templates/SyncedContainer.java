package de.take_weiland.mods.commons.templates;

import net.minecraft.inventory.IInventory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface SyncedContainer<T extends IInventory> extends SCContainer<T> {

	void readSyncData(DataInputStream in) throws IOException;

	boolean writeSyncData(DataOutputStream out) throws IOException;
	
}
