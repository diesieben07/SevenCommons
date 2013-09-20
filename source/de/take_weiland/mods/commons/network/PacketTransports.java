package de.take_weiland.mods.commons.network;

public final class PacketTransports {

	private PacketTransports() { }
	
	public static PacketTransport withPacket250(String channel, PacketType[] packets) {
		return new Packet250Transport(channel, packets);
	}
	
	public static PacketTransport withPacket131(Object mod, PacketType[] packets) {
		return new Packet131Transport(mod, packets);
	}
}
