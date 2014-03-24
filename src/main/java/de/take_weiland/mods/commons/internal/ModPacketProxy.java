package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.SimplePacketType;

public interface ModPacketProxy<TYPE extends Enum<TYPE> & SimplePacketType & SimplePacketTypeProxy> {

	public static final String GET_TYPE = "_sc$getPacketType";

	TYPE  _sc$getPacketType();

}
