package de.take_weiland.mods.commons.network;

import java.io.InputStream;
import java.util.Map;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;

public interface MultipartPacket extends ModPacket {

	MultipartPacketType type();
	
	Packet[] make();
	
	public static interface MultipartPacketType extends PacketType {
		
		Map<INetworkManager, InputStream[]> tracker();
		
	}
	
}
