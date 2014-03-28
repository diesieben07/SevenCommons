package de.take_weiland.mods.commons.net;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> extends PacketFactory<TYPE> {

	SimplePacket make(WritableDataBufImpl<TYPE> buf);

	PacketBuilder response(int capacity);

}
