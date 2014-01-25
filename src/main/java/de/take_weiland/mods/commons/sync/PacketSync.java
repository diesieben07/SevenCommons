package de.take_weiland.mods.commons.sync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.Packets;

public class PacketSync extends DataPacket {

	@Override
	protected void write(DataOutputStream out) throws IOException {
		throw new UnsupportedOperationException("This should never be called!");
	}

	@Override
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		SyncType type = Packets.readEnum(in, SyncType.class);
		Object readObj = type.recreate(player, in);
		if (readObj instanceof SyncedObject) {
			((SyncedObject) readObj)._sc_sync_read(in);
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) { }

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.SYNC;
	}
}
