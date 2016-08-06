package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.PacketHandlerBase;

/**
 * <p>Stores data about a packet class.</p>
 *
 * @author diesieben07
 */
public final class PacketData {

    public final PacketHandlerBase handler;
    public final byte characteristics;
    public final byte packetId;
    public final String channel;

    public PacketData(PacketHandlerBase handler, byte characteristics, byte packetId, String channel) {
        this.handler = handler;
        this.characteristics = characteristics;
        this.packetId = packetId;
        this.channel = channel;
    }
}
