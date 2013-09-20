package de.take_weiland.mods.commons.network;

import java.util.Arrays;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import de.take_weiland.mods.commons.util.CollectionUtils;
import de.take_weiland.mods.commons.util.Sides;

abstract class PacketTransportAbstract implements PacketTransport {
	
	protected final Class<? extends ModPacket>[] packets;

	protected PacketTransportAbstract(PacketType[] types) {
		@SuppressWarnings("unchecked")
		Class<? extends ModPacket>[] packets = new Class[types.length];
		for (PacketType type : types) {
			int packetId = type.packetId();
			if (!CollectionUtils.arrayIndexExists(packets, packetId)) {
				packets = Arrays.copyOf(packets, packetId + 1);
			}
			packets[packetId] = type.packetClass();
		}
		this.packets = packets;
	}
	
	protected final ModPacket newPacket(int packetId) {
		Class<? extends ModPacket> packetClass = CollectionUtils.safeArrayAccess(packets, packetId);
		if (packetClass == null) {
			throw new NetworkException(String.format("Unknown PacketId: %d", Integer.valueOf(packetId)));
		}
		
		try {
			return packetClass.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new NetworkException(String.format("Failed to instantiate Packet class %s", packetClass.getName()), e);
		}
	}
	
	protected final void finishPacketRecv(int packetId, EntityPlayer player, byte[] data, int dataOffset) {
		ModPacket packet = newPacket(packetId);
		packet.handleData(data, dataOffset);
		Side side = Sides.logical(player);
		if (!packet.isValidForSide(side)) {
			throw new NetworkException(String.format("Received Packet %s for invalid side %s", packet.getClass().getName(), side));
		}
		packet.execute(player, side);
	}
}