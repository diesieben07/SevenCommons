package de.take_weiland.mods.commons.internal.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketSync extends ModPacket {

	@Override
	protected void write(MCDataOutputStream out) {
		throw new IllegalStateException();
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		SyncMethod method = in.readEnum(SyncMethod.class);
		Object o = method.readData(in, player);
		if (o instanceof AutoSyncedObject) {
			((AutoSyncedObject) o)._sc$sync$read(in, false);
		} else {
			throw new ProtocolException("Received invalid SyncedObject");
		}
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException { }
}
