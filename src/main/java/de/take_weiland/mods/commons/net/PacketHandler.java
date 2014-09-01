package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
public interface PacketHandler {

	MCDataOutputStream createStream(int packetId);

	MCDataOutputStream createStream(int packetId, int initialCapacity);

	SimplePacket makePacket(MCDataOutputStream stream);

}
