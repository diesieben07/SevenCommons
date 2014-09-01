package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.SCReflector;
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

	public static void sendTo(Packet packet, EntityPlayer player) {
		checkNotClient(player).playerNetServerHandler.sendPacketToPlayer(packet);
	}

	public static void sendTo(Packet packet, Iterable<? extends EntityPlayer> players) {
		for (EntityPlayer player : players) {
			checkNotClient(player).playerNetServerHandler.sendPacketToPlayer(packet);
		}
	}

	public static void sendTo(Packet packet, EntityPlayer... players) {
		for (EntityPlayer player : players) {
			checkNotClient(player).playerNetServerHandler.sendPacketToPlayer(packet);
		}
	}

	/**
	 * Sends the packet to all players tracking the entity. If the entity is a player, does not include the player itself.
	 */
	public static void sendToAllTracking(Packet packet, Entity entity) {
		checkNotClient(entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, packet);
	}

	/**
	 * Same as {@link #sendToAllTracking(net.minecraft.network.packet.Packet, net.minecraft.entity.Entity)}, but includes the player itself if the entity is a player.
	 */
	public static void sendToAllAssociated(Packet packet, Entity entity) {
		checkNotClient(entity.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(entity, packet);
	}

	/**
	 * Sends the packet to all players tracking the given TileEntity, that is tracking the chunk the TE is in.
	 */
	public static void sendToAllTracking(Packet packet, TileEntity te) {
		sendToAllTrackingChunk(packet, te.worldObj, te.xCoord >> 4, te.zCoord >> 4);
	}

	/**
	 * Sends the packet to all players tracking the chunk at the given coordinates.
	 */
	public static void sendToAllTrackingChunk(Packet packet, World world, int chunkX, int chunkZ) {
		PlayerInstance pi = checkNotClient(world).getPlayerManager().getOrCreateChunkWatcher(chunkX, chunkZ, false);
		if (pi != null) {
			pi.sendToAllPlayersWatchingChunk(packet);
		}
	}

	/**
	 * <p>Sends the Packet to all players viewing the given container.</p>
	 * <p>Warning: Due to Minecraft's interal design a new Container get's created for every player, even if they are watching the same inventory.
	 * This method does <i>not</i> check for players viewing the inventory. As in: this method usually sends the packet to only one player.</p>
	 */
	public static void sendToViewing(Packet packet, Container container) {
//		Could use the shorter
//		Packets.sendPacketToPlayers(packet, Iterators.filter(crafters.iterator(), EntityPlayerMP.class));
//		but this version here avoids the object spam of the above
		List<ICrafting> crafters = SCReflector.instance.getCrafters(container);
		int len = crafters.size();
		for (int i = 0; i < len; ++i) {
			ICrafting crafter = crafters.get(i);
			if (crafter instanceof EntityPlayerMP) {
				PacketDispatcher.sendPacketToPlayer(packet, (Player) crafter);
			}
		}
	}

	public static void sendToAllNear(Packet packet, World world, double x, double y, double z, double radius) {
		List<EntityPlayerMP> players = Players.allIn(checkNotClient(world));
		int len = players.size();
		radius *= radius;

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < len; ++i) {
			EntityPlayerMP player = players.get(i);
			double dx = x - player.posX;
			double dy = y - player.posY;
			double dz = z - player.posZ;
			if (dx * dx + dy * dy + dz * dz < radius) {
				player.playerNetServerHandler.sendPacketToPlayer(packet);
			}
		}
	}

	private static EntityPlayerMP checkNotClient(EntityPlayer player) {
		if (player.worldObj.isRemote) {
			throw new IllegalArgumentException("Tried to send packet using a client side player");
		}
		return (EntityPlayerMP) player;
	}

	private static WorldServer checkNotClient(World world) {
		if (world.isRemote) {
			throw new IllegalArgumentException("Tried to send packet using a client world");
		}
		return (WorldServer) world;
	}
}
