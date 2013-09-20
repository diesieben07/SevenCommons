package de.take_weiland.mods.commons.network;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.util.Sides;

public final class Packets {

	private Packets() { }

	public static void sendPacketToPlayer(Packet p, EntityPlayer player) {
		PacketDispatcher.sendPacketToPlayer(p, (Player)player);
	}
	
	public static void sendPacketToPlayers(Packet p, Iterable<? extends EntityPlayer> players) {
		sendPacketToPlayers(p, players.iterator());
	}
	
	public static void sendPacketToPlayers(Packet p, Iterator<? extends EntityPlayer> players) {
		while (players.hasNext()) {
			sendPacketToPlayer(p, players.next());
		}
	}
	
	public static void sendPacketToPlayers(Packet p, EntityPlayer... players) {
		for (EntityPlayer player : players) {
			PacketDispatcher.sendPacketToPlayer(p, (Player)player);
		}
	}

	public static void sendPacketToAllTracking(Packet p, Entity entity) {
		if (Sides.logical(entity).isServer()) {
			((WorldServer)entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, p);
		}
	}
	
}
