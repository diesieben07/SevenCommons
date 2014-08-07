package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.NetworkRegistry;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public final class PacketHandlerBuilder {

	private final String channel;
	private final Map<Integer, Class<? extends ModPacket>> packets = Maps.newHashMap();
	private int nextId = 0;

	PacketHandlerBuilder(String channel) {
		this.channel = channel;
	}

	public PacketHandlerBuilder register(Class<? extends ModPacket> packet) {
		return register(packet, nextId);
	}

	public PacketHandlerBuilder register(Class<? extends ModPacket> packet, int id) {
		checkNotNull(packet, "packet");
		checkArgument(id >= 0, "id must be >= 0");
		packets.put(id, packet);
		nextId = id + 1;
		return this;
	}

	public void build() {
		NetworkRegistry.instance().registerChannel(new FMLPacketHandlerImpl(channel, ImmutableBiMap.copyOf(packets)), channel);
	}

}
