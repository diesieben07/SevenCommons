package de.take_weiland.mods.commons.network;

import java.io.IOException;

public final class PacketTransports {

	private PacketTransports() { }
	
	public static <E extends Enum<E> & PacketType> PacketTransport withPacket250(String channel, Class<E> typeClass) {
		return new Packet250Transport(channel, typeClass);
	}
	
	public static <E extends Enum<E> & PacketType> PacketTransport withPacket131(Object mod, Class<E> typeClass) {
		return new Packet131Transport(mod, typeClass);
	}
	
	static final RuntimeException tooBigException(int size, int maxSize) {
		return new NetworkException(String.format("Illegal Packet size %d, must be max. %d", Integer.valueOf(size), Integer.valueOf(maxSize)));
	}
	
	static final RuntimeException wrapIOException(ModPacket packet, IOException e) {
		return new NetworkException(String.format("Unexpected IOException during Packet writing for packet %s", packet.getClass().getCanonicalName()), e);
	}
	
	static final RuntimeException wrapIOException(PacketType packet, IOException e) {
		return new NetworkException(String.format("Unexpected IOException during Packet writing for packet %s", packet.packetClass().getCanonicalName()), e);
	}
}
