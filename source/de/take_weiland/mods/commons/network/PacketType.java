package de.take_weiland.mods.commons.network;

public interface PacketType {

	int packetId();
	
	PacketTransport transport();
	
	Class<? extends ModPacket> packetClass();
	
}
