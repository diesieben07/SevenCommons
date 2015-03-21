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
    public void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		SyncType type = in.readEnum(SyncType.class);
        Object object = type.readObject(player, in);
        SyncerCompanion companion = object == null ? null : ((SyncedObjectProxy) object)._sc$getCompanion();
        if (companion != null) {
			companion.read(object, in);
		} else {
			logger.warning("Received invalid object for syncing: " + object);
		}
	}

	@Override
    public void execute(EntityPlayer player, Side side) throws ProtocolException { }

	@Override
    public void write(MCDataOutputStream out) {
		throw new AssertionError();
	}
}
