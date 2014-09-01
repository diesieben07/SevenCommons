package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.network.packet.Packet;

/**
 * A target for packets. Only the special value {@link #SERVER} may be used for sending to the server.
 */
public interface PacketTarget {

	void send(Packet packet);

	public static final PacketTarget SERVER = new PacketTarget() {

		@Override
		public void send(Packet packet) {
			PacketDispatcher.sendPacketToServer(packet);
		}

	};

	public static final PacketTarget ALL_PLAYERS = new PacketTarget() {

		@Override
		public void send(Packet packet) {
			PacketDispatcher.sendPacketToAllPlayers(packet);
		}

	};

}
