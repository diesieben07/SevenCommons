package de.take_weiland.mods.commons.network;

import net.minecraft.network.packet.Packet;
import cpw.mods.fml.common.network.PacketDispatcher;

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
