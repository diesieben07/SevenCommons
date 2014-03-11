package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

public final class PacketSync extends ModPacket {

	@Override
	protected void write(WritableDataBuf out) {
		throw new UnsupportedOperationException("This should never be called!");
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		SyncType type = DataBuffers.readEnum(in, SyncType.class);
		Object readObj = type.recreate(player, in);
		if (readObj instanceof SyncedObject) {
			((SyncedObject) readObj)._sc$syncRead(in);
		}
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

}
