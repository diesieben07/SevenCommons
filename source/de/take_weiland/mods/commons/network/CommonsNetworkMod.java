package de.take_weiland.mods.commons.network;

import java.util.Map;


public interface CommonsNetworkMod {

	boolean useTinyPackets();
	
	String getChannel();
	
	Map<Integer, Class<? extends ModPacket>> getPacketList();
	
}
