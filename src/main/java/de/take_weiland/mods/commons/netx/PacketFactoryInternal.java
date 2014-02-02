package de.take_weiland.mods.commons.netx;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> {

	SimplePacket make(WritableDataBufImpl<TYPE> buf);
	
}
