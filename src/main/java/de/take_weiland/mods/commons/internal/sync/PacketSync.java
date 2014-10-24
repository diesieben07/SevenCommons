package de.take_weiland.mods.commons.internal.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SevenCommonsLoader;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketSync extends ModPacket {

	private static final Logger logger = SevenCommonsLoader.scLogger("SyncSystem");

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		SyncType type = in.readEnum(SyncType.class);
		Object object = type.readObject(player, in);
		if (object instanceof SyncedObjectProxy) {
			if (((SyncedObjectProxy) object)._sc$sync$read(in) != 0) {
				throw new ProtocolException("Invalid SyncIndex received for " + object);
			}
		} else {
			logger.warning("Received invalid object for syncing: " + object);
		}
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException { }

	@Override
	protected void write(MCDataOutputStream out) {
		throw new AssertionError();
	}
}
