package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketTypeId extends ModPacket {

	private Class<?> clazz;
	private int id;

	public PacketTypeId(Class<?> clazz, int id) {
		this.clazz = clazz;
		this.id = id;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeString(clazz.getName());
		out.writeInt(id);
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		try {
			clazz = Class.forName(in.readString());
		} catch (ClassNotFoundException e) {
			throw PacketTypeIds.invalidClass(e);
		}
		id = in.readInt();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		SyncingManager.injectTypeId(clazz, id);
	}
}
