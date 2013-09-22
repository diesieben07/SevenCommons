package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.Packet;

public interface PacketTransport {

	Packet make(ModPacket packet);
	
	Packet[] makeMulti(ModPacket packet);
	
	int maxPacketSize();
	
}
