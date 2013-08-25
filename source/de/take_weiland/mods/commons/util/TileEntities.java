package de.take_weiland.mods.commons.util;

import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import de.take_weiland.mods.commons.internal.PacketTileEntitySync;
import de.take_weiland.mods.commons.templates.SyncedTileEntity;

public final class TileEntities {

	private TileEntities() { }

	public static <T extends TileEntity & SyncedTileEntity> Packet getSyncPacket(T te) {
		return new PacketTileEntitySync(te).getVanillaPacket();
	}
	
}
