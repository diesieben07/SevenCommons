package de.take_weiland.mods.commons.net;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.NetworkRegistry;
import de.take_weiland.mods.commons.internal.ModPacketProxy;

import java.util.BitSet;
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
	private final BitSet ids = new BitSet(64);

	PacketHandlerBuilder(String channel) {
		this.channel = channel;
	}

	/**
	 * <p>Register a packet class. This method uses the next free ID, which is 0 if this is the first registered packet.</p>
	 * @param packet the packet class
	 * @return this builder, for convenience
	 */
	public PacketHandlerBuilder register(Class<? extends ModPacket> packet) {
		return register(packet, ids.nextClearBit(0));
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
		checkArgument(!ids.get(id), "id already taken");
		packets.put(id, packet);
		ids.set(id);
		return this;
	}

	/**
	 * <p>Finish registering your packets and register the resulting PacketHandler to FML's network system.</p>
	 */
	public PacketHandler build() {
		FMLPacketHandlerImpl handler = new FMLPacketHandlerImpl(channel, ImmutableBiMap.copyOf(packets));
		for (Class<? extends ModPacket> packet : packets.values()) {
			try {
				ModPacketProxy instance = (ModPacketProxy) packet.newInstance();
				checkArgument(instance._sc$handler() == null, "Packet %s already in use with another channel!", packet.getSimpleName());
				instance._sc$setHandler(handler);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(String.format("Packet transformer failed on %s", packet.getName()));
			}
		}

		NetworkRegistry.instance().registerChannel(handler, channel);
		return handler;
	}

}
