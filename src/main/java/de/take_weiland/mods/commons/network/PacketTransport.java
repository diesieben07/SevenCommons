package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.Packet;

import java.io.DataOutput;

public interface PacketTransport {

	/**
	 * prepare the given DataOutput for writing of the given PacketType
	 * @param out
	 * @param type
	 */
	void prepareOutput(DataOutput out, PacketType type);
	
	/**
	 * create a Packet from the given data and PacketType. data must be created using {@link #prepareOutput(DataOutput, PacketType) prepareOutput}
	 * @param data
	 * @param type
	 * @return
	 */
	Packet make(byte[] data, PacketType type);
	
	Packet make(ModPacket packet);
	
	Packet[] makeMulti(ModPacket packet);
	
	int maxPacketSize();
	
}
