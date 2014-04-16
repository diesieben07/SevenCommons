package de.take_weiland.mods.commons.net;

interface PacketFactoryInternal<TYPE extends Enum<TYPE>> extends PacketFactory<TYPE> {

	SimplePacket build(PacketBufferImpl<TYPE> buffer);

	void registerCallback(SimplePacket wrapper, ModPacket.WithResponse<?> packet);

}
