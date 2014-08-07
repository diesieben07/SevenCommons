package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.NetworkRegistry;
import de.take_weiland.mods.commons.internal.ModPacketProxy;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>A helper class for registering your Packet classes.</p>
 *
 * @see de.take_weiland.mods.commons.net.Network#newChannel(String)
 * @see de.take_weiland.mods.commons.net.ModPacket
 *
 * @author diesieben07
 */
public final class PacketHandlerBuilder {

	private final String channel;
	private final Map<Integer, Class<? extends ModPacket>> packets = Maps.newHashMap();
	private int nextId = 0;

	PacketHandlerBuilder(String channel) {
		this.channel = channel;
	}

	/**
	 * <p>Register a packet class. This method uses the next ID after the last registered one, or 0 if this is the first
	 * registered packet.</p>
	 * @param packet the packet class
	 * @return this builder, for convenience
	 */
	public PacketHandlerBuilder register(Class<? extends ModPacket> packet) {
		return register(packet, nextId);
	}

	/**
	 * <p>Register a packet class with the given ID.</p>
	 * @param packet the packet class
	 * @param id the ID to use for this packet
	 * @return this builder, for convenience
	 */
	public PacketHandlerBuilder register(Class<? extends ModPacket> packet, int id) {
		checkNotNull(packet, "packet");
		checkArgument(id >= 0, "id must be >= 0");
		checkArgument(!packets.containsKey(id), "id already taken");
		try {
			checkArgument(((ModPacketProxy) packet.newInstance())._sc$handler() == null, "Packet already in use with another channel!");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(String.format("Packet transformer failed on %s", packet.getName()));
		}
		packets.put(id, packet);
		nextId = id + 1;
		return this;
	}

	/**
	 * <p>Finish registering your packets and register the resulting PacketHandler to FML's network system.</p>
	 */
	public void build() {
		NetworkRegistry.instance().registerChannel(new FMLPacketHandlerImpl(channel, ImmutableBiMap.copyOf(packets)), channel);
	}

}
