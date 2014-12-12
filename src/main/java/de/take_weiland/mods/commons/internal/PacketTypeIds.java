package de.take_weiland.mods.commons.internal;

import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.properties.Types;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.Map;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketTypeIds extends ModPacket {

	private Map<Integer, Class<?>> map;

	public PacketTypeIds(Map<Integer, Class<?>> map) {
		this.map = map;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeVarInt(map.size());
		for (Map.Entry<Integer, Class<?>> entry : map.entrySet()) {
			out.writeString(entry.getValue().getName());
			out.writeInt(entry.getKey());
		}
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		int size = in.readVarInt();
		map = Maps.newHashMapWithExpectedSize(size);
		for (int i = 0; i < size; i++) {
			String id = in.readString();
			Class<?> clazz = Types.getClass(id);
			if (clazz == null) {
				throw new ProtocolException("Received unknown TypeID " + id);
			}

			map.put(in.readInt(), clazz);
		}
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		Types.injectNumericalIDs(map);
	}
}
