package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.PacketFactory;

public interface PacketWithFactory<TYPE extends Enum<TYPE>> {

	public static final String GET_FACTORY = "_sc$getPacketFactory";
	public static final String GET_TYPE = "_sc$getPacketType";


	PacketFactory<TYPE> _sc$getPacketFactory();
	
	TYPE _sc$getPacketType();
	
}
