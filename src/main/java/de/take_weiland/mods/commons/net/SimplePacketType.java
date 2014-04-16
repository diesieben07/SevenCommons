package de.take_weiland.mods.commons.net;

/**
 * Implement this on your Packet type enum for class-based Packet handling.
 * @see de.take_weiland.mods.commons.net.ModPacket
 */
public interface SimplePacketType {

	/**
	 * @return the packet class corresponding to this Packet types
	 */
	Class<? extends ModPacketBase> packet();
	
}
