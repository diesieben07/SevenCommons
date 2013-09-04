package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import de.take_weiland.mods.commons.network.ModPacket;

public final class Syncing {

	private Syncing() { }
	
	public static <T extends Container & Synced> ModPacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.CONTAINER, forceSync);
	}
	
	public static <T extends Entity & Synced> ModPacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.ENTITY, forceSync);
	}
	
	public static <T extends TileEntity & Synced> ModPacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.TILE_ENTITY, forceSync);
	}
	
	public static <T extends Synced> ModPacket getSyncPacket(T synced, SyncedType<? super T> type, boolean forceSync) {
		return syncImpl(synced, type, forceSync);
	}
	
	public static void restoreSyncData(SyncedFieldAccessor accessor, ByteArrayDataInput in) {
		boolean lastField;
		do {
			byte fieldAndIsLast = in.readByte();
			lastField = (fieldAndIsLast & 0x80) != 0;
			int fieldIndex = fieldAndIsLast & 0x7F;
			accessor.receiveField(fieldIndex, in);
		} while (!lastField);
		
		accessor.uploadSyncedFields();
	}
	
	private static <T extends Synced> ModPacket syncImpl(T synced, SyncedType<? super T> type, boolean forceSync) {
		SyncedFieldAccessor accessor = (SyncedFieldAccessor)synced;
		int fields = accessor.getFieldCount();
		
		ByteArrayDataOutput out = null;
		
		accessor.downloadSyncedFields();
		
		for (int field = 0; field < fields; field++) {
			if (forceSync || accessor.needsUpdate(field)) {
				if (out == null) {
					out = type.provideDataOutput();
					type.outputInstanceInfo(synced, out);
				}
				boolean lastField = field == fields - 1;
				out.writeByte((field & 0x7F) | (lastField ? 0x80 : 0x00));
				accessor.sendField(field, out);
			}
		}
		
		if (!forceSync) {
			accessor.updateFields();
		}
		
		return out == null ? ModPacket.DUMMY_PACKET : type.providePacket(out);
	}

}
