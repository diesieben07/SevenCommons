package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import de.take_weiland.mods.commons.network.SendablePacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public final class Syncing {

	private Syncing() { }
	
	public static <T extends Container & Synced> SendablePacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.CONTAINER, forceSync);
	}
	
	public static <T extends Entity & Synced> SendablePacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.ENTITY, forceSync);
	}
	
	public static <T extends TileEntity & Synced> SendablePacket getSyncPacket(T synced, boolean forceSync) {
		return syncImpl(synced, SyncedType.TILE_ENTITY, forceSync);
	}
	
	public static <T extends Synced> SendablePacket getSyncPacket(T synced, SyncedType<? super T> type, boolean forceSync) {
		return syncImpl(synced, type, forceSync);
	}
	
	public static void restoreSyncData(SyncedFieldAccessor accessor, MinecraftDataInput in) {
		do {
			byte field = in.readByte();
			if (field < 0) {
				break;
			}
			accessor.receiveField(field, in);
		} while (true);
		
		accessor.uploadSyncedFields();
	}
	
	private static <T extends Synced> SendablePacket syncImpl(T synced, SyncedType<? super T> type, boolean forceSync) {
		SyncedFieldAccessor accessor = (SyncedFieldAccessor)synced;
		int fields = accessor.getFieldCount();
		
		MinecraftDataOutput out = null;
		
		accessor.downloadSyncedFields();
		
		for (int field = 0; field < fields; field++) {
			if (forceSync || accessor.needsUpdate(field)) {
				if (out == null) {
					out = type.provideDataOutput();
					type.outputInstanceInfo(synced, out);
				}
				out.writeByte(field);
				accessor.sendField(field, out);
			}
		}
		
		if (out != null) {
			out.writeByte(-1);
		}
		
		if (!forceSync) {
			accessor.updateFields();
		}
		
		return out == null ? SendablePacket.DUMMY : type.providePacket(out);
	}

}
