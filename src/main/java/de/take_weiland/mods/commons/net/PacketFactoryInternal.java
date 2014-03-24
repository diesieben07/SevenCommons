package de.take_weiland.mods.commons.net;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> extends PacketFactory<TYPE> {

	SimplePacket make(WritableDataBufImpl<TYPE> buf);

	<T> PacketBuilder builderWithResponseHandler(TYPE type, int capacity, ModPacket.WithResponse<T> packet, PacketResponseHandler<? super T> handler);

	PacketBuilder response();

}
