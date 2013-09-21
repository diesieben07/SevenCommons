package de.take_weiland.mods.commons.network;

public interface PacketType {

	/**
	 * the packetId, must be 0 through 127, numbers higher or lower will cause problems
	 * @return
	 */
	int packetId();
	
	PacketTransport transport();
	
	Class<? extends ModPacket> packetClass();
	
}
