package de.take_weiland.mods.commons.netx;

public interface SimplePacketType<SELF extends Enum<SELF> & SimplePacketType<SELF>> {

	Class<? extends ModPacket<SELF>> packet();
	
}
