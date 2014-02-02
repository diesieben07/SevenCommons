package de.take_weiland.mods.commons.net;

public interface PacketWithFactory<TYPE extends Enum<TYPE>> {

	PacketFactory<TYPE> _sc_getFactory();
	
	TYPE _sc_getType();
	
}
