package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.PacketFactory;

public interface PacketWithFactory<TYPE extends Enum<TYPE>> {

	PacketFactory<TYPE> _sc_getFactory();
	
	TYPE _sc_getType();
	
}
