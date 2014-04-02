package de.take_weiland.mods.commons.net;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> extends PacketFactory<TYPE> {

	SimplePacket build(PacketBufferImpl<TYPE> buffer);

	PacketBuilder.ForResponse createResponse(int capacity, PacketBufferImpl<TYPE> input);

	void onResponseHandlerSet(PacketBufferImpl<TYPE> buffer, PacketResponseHandler handler);

}
