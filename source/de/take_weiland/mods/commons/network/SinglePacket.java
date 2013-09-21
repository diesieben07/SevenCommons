package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.Packet;

public interface SinglePacket extends ModPacket {

	Packet make();
	
}
