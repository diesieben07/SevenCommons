package de.take_weiland.mods.commons.net;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> {

	SimplePacket make(WritableDataBufImpl<TYPE> buf);

}
