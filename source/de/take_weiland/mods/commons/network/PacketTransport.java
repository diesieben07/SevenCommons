package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.Packet;

public interface PacketTransport {

	Packet toVanilla(ModPacket packet);
	
}
