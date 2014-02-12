package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.Internal;

@Internal
public interface PacketWithFactory<TYPE extends Enum<TYPE>> {

	PacketFactory<TYPE> _sc_getFactory();
	
	TYPE _sc_getType();
	
}
