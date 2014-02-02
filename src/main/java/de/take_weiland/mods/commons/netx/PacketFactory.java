package de.take_weiland.mods.commons.netx;

public interface PacketFactory<TYPE extends Enum<TYPE>> {

	PacketBuilder builder(TYPE t);
	PacketBuilder builder(TYPE t, int capacity);
	
}
