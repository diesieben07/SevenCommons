package de.take_weiland.mods.commons.network;

import java.io.IOException;
import java.util.Map;

import net.minecraft.network.packet.Packet;
import cpw.mods.fml.relauncher.ReflectionHelper;

public final class PacketTransports {

	private PacketTransports() { }
	
	public static PacketTransport withPacket250(String channel, PacketType[] packets) {
		return new Packet250Transport(channel, packets);
	}
	
	public static PacketTransport withPacket131(Object mod, PacketType[] packets) {
		return new Packet131Transport(mod, packets);
	}
	
	static final Map<Class<?>, Integer> classToIdMap;
	
	static {
		classToIdMap = ReflectionHelper.getPrivateValue(Packet.class, null, 1);
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
