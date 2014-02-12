package de.take_weiland.mods.commons.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.internal.SCPacket;
import de.take_weiland.mods.commons.internal.SyncType;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

@Internal
public final class PacketSync extends SCPacket {

	@Override
	protected void write(WritableDataBuf out) {
		throw new UnsupportedOperationException("This should never be called!");
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		SyncType type = Packets.readEnum(in, SyncType.class);
		Object readObj = type.recreate(player, in);
		if (readObj instanceof SyncedObject) {
			((SyncedObject) readObj)._sc_sync_read(in);
		}
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

}
