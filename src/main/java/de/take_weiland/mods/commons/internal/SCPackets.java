package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.SimplePacketType;

/**
 * @author diesieben07
 */
public enum SCPackets implements SimplePacketType {

	INV_NAME(PacketInventoryName.class);

	private final Class<? extends ModPacket> packet;

	SCPackets(Class<? extends ModPacket> packet) {
		this.packet = packet;
	}

	@Override
	public Class<? extends ModPacket> packet() {
		return packet;
	}

}
