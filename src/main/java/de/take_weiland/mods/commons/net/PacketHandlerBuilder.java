package de.take_weiland.mods.commons.net;

import com.google.common.collect.Maps;
import cpw.mods.fml.common.network.NetworkRegistry;
import de.take_weiland.mods.commons.internal.FMLPacketHandlerImpl;

import java.util.BitSet;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

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
	private Map<Class<? extends ModPacket>, Integer> packets = Maps.newHashMap();
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
		checkNotBuilt();
        checkNotNull(packet, "packet");
		checkArgument(id >= 0, "id must be >= 0");
		checkArgument(!ids.get(id), "id already taken");
		packets.put(packet, id);
		ids.set(id);
		return this;
	}

    private void checkNotBuilt() {
        checkState(packets != null, "PacketHandlerBuilder already used");
    }

	/**
	 * <p>Finish registering your packets and register the resulting PacketHandler to FML's network system.</p>
	 */
	public PacketHandler build() {
		FMLPacketHandlerImpl handler = new FMLPacketHandlerImpl(channel, packets);
        packets = null;
		NetworkRegistry.instance().registerChannel(handler, channel);
		return handler;
	}

}
