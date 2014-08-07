package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.SCReflector;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.management.PlayerInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class for sending Packets around, in addition to {@link cpw.mods.fml.common.network.PacketDispatcher}.
 * Most methods should be self-explanatory.
 */
public final class Packets {

	private Packets() {
	}

	public static void sendToServer(Packet p) {
		SCModContainer.proxy.sendPacketToServer(p);
	}

	public static void sendPacketToPlayer(Packet p, EntityPlayer player) {
		try {
			((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(p);
		} catch (ClassCastException e) {
			throw clientPlayer(e);
		}
	}

	private static IllegalArgumentException clientPlayer(ClassCastException e) {
		return new IllegalArgumentException("Cannot send Packet to a client player!", e);
	}

	public static void sendPacketToPlayers(Packet p, Iterable<? extends EntityPlayer> players) {
		sendPacketToPlayers(p, players.iterator());
	}

	public static void sendPacketToPlayers(Packet p, Iterator<? extends EntityPlayer> players) {
		try {
			while (players.hasNext()) {
				((EntityPlayerMP) players.next()).playerNetServerHandler.sendPacketToPlayer(p);
			}
		} catch (ClassCastException e) {
			throw clientPlayer(e);
		}
	}

	public static void sendPacketToPlayers(Packet p, EntityPlayer... players) {
		try {
			for (EntityPlayer player : players) {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacketToPlayer(p);
			}
		} catch (ClassCastException e) {
			throw clientPlayer(e);
		}
	}

	/**
	 * Sends the packet to all players tracking the entity. If the entity is a player, does not include the player itself.
	 */
	public static void sendPacketToAllTracking(Packet p, Entity entity) {
		sendPacketToPlayers(p, Entities.getTrackingPlayers(entity));
	}

	/**
	 * Same as {@link #sendPacketToAllTracking(net.minecraft.network.packet.Packet, net.minecraft.entity.Entity)}, but includes the player itself if the entity is a player.
	 */
	public static void sendPacketToAllAssociated(Packet p, Entity entity) {
		sendPacketToAllTracking(p, entity);
		if (entity instanceof EntityPlayerMP) {
			sendPacketToPlayer(p, (EntityPlayer) entity);
		}
	}

	/**
	 * Sends the packet to all players tracking the given TileEntity, that is tracking the chunk the TE is in.
	 */
	public static void sendPacketToAllTracking(Packet p, TileEntity te) {
		sendPacketToAllTrackingChunk(p, te.worldObj, te.xCoord >> 4, te.zCoord >> 4);
	}

	/**
	 * Sends the packet to all players tracking the chunk at the given coordinates.
	 */
	public static void sendPacketToAllTrackingChunk(Packet p, World w, int chunkX, int chunkZ) {
		if (Sides.logical(w).isServer()) {
			PlayerInstance pi = ((WorldServer) w).getPlayerManager().getOrCreateChunkWatcher(chunkX, chunkZ, false);
			if (pi != null) {
				pi.sendToAllPlayersWatchingChunk(p);
			}
		}
	}

	/**
	 * <p>Sends the Packet to all players viewing the given container.</p>
	 * <p>Warning: Due to Minecraft's interal design a new Container get's created for every player, even if they are watching the same inventory.
	 * This method does <i>not</i> check for players viewing the inventory. As in: this method usually sends the packet to only one player.</p>
	 */
	public static void sendPacketToViewing(Packet packet, Container c) {
//		Could use the shorter
//		Packets.sendPacketToPlayers(packet, Iterators.filter(crafters.iterator(), EntityPlayerMP.class));
//		but this version here avoids the object spam of the above
		List<ICrafting> crafters = SCReflector.instance.getCrafters(c);
		int len = crafters.size();
		for (int i = 0; i < len; ++i) {
			ICrafting crafter = crafters.get(i);
			if (crafter instanceof EntityPlayerMP) {
				PacketDispatcher.sendPacketToPlayer(packet, (Player) crafter);
			}
		}
	}
}
