package de.take_weiland.mods.commons.net;

import net.minecraft.network.packet.Packet;

/**
 * <p>A target for packets. Only the special value {@link #SERVER} may be used for sending to the server.</p>
 */
public interface PacketTarget {

	void send(Packet packet);

	public static final PacketTarget SERVER = new PacketTarget() {

		@Override
		public void send(Packet packet) {
			Packets.sendToServer(packet);
		}

	};

	public static final PacketTarget ALL_PLAYERS = new PacketTarget() {

		@Override
		public void send(Packet packet) {
			Packets.sendToAll(packet);
		}

	};

}
