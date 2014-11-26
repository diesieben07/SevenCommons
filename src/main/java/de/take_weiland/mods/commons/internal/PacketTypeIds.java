package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.sync.SyncingManager;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.Map;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketTypeIds extends ModPacket {

	private Map<Class<?>, Integer> map;

	public PacketTypeIds(Map<Class<?>, Integer> map) {
		this.map = map;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeVarInt(map.size());
		for (Map.Entry<Class<?>, Integer> entry : map.entrySet()) {
			out.writeString(entry.getKey().getName());
			out.writeInt(entry.getValue());
		}
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		int size = in.readVarInt();
		map = Maps.newHashMapWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			try {
				map.put(Class.forName(in.readString()), in.readInt());
			} catch (ClassNotFoundException e) {
				throw invalidClass(e);
			}
		}
	}

	static ProtocolException invalidClass(ClassNotFoundException e) {
		return new ProtocolException("Received unknown class to be synced", e);
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		SyncingManager.injectTypeIDs(map);
	}
}
