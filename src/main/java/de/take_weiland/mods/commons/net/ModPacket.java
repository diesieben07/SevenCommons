package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.io.IOException;
import java.util.List;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every type of Packet you have.</p>
 * <p>Register your packet classes with {@link de.take_weiland.mods.commons.net.Network#newChannel(String)}.</p>
 * <p>To send a packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket} like this:<br />
 * {@code new ExamplePacket("whatever").sendToServer();}<br />
 * {@code new OtherPacket(someObject).sendToPlayer(somePlayer).sendToPlayer(otherPlayer);}</p>
 * <p>Use {@link de.take_weiland.mods.commons.net.PacketDirection} to specify a valid direction this packet can be send.</p>
 */
public abstract class ModPacket implements SimplePacket {

	/**
	 * <p>Write your packet data to the stream. A stream containing the same data will be passed to
	 * {@link #read(MCDataInputStream, net.minecraft.entity.player.EntityPlayer, cpw.mods.fml.relauncher.Side)} when the
	 * packet is received.</p>
	 * @param out the stream
	 */
	protected abstract void write(MCDataOutputStream out);

	/**
	 * <p>Read your packet data from the stream. The stream contains the data written in {@link #write(MCDataOutputStream)}.</p>
	 * <p>On the server, the player is the player sending the packet, on the client it is the client player (receiving the packet).</p>
	 * @param in the stream
	 * @param player the context player
	 * @param side the logical side receiving the packet
	 * @throws IOException if an IOException occurs while reading the data
	 * @throws ProtocolException if the data received violates the protocol
	 */
	protected abstract void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException;

	/**
	 * <p>Execute this packet's action. This method is called when the packet is received, after the data has been read
	 * with {@link #read(MCDataInputStream, net.minecraft.entity.player.EntityPlayer, cpw.mods.fml.relauncher.Side)}.</p>
	 * @param player the context player (see {@link #read(MCDataInputStream, net.minecraft.entity.player.EntityPlayer, cpw.mods.fml.relauncher.Side)}
	 * @param side the logical side receiving the packet
	 * @throws ProtocolException if the data received violates the protocol
	 */
	protected abstract void execute(EntityPlayer player, Side side) throws ProtocolException;

	/**
	 * <p>An estimate of the size of this packet's data in bytes. Used to pre-size the byte buffer that this packet is
	 * written to.</p>
	 * @return an estimated size
	 */
	protected int expectedSize() {
		return 32;
	}

	// private implementation

	Packet mcPacket;
	private Packet build() {
		return mcPacket == null ? (mcPacket = ((ModPacketProxy) this)._sc$handler().buildPacket(this)) : mcPacket;
	}

	@Override
	public final SimplePacket sendTo(PacketTarget target) {
		target.send(build());
		return this;
	}

	@Override
	public final SimplePacket sendToServer() {
		SCModContainer.proxy.sendPacketToServer(build());
		return this;
	}

	@Override
	public final SimplePacket sendTo(EntityPlayer player) {
		checkNotClient(player).playerNetServerHandler.sendPacketToPlayer(build());
		return this;
	}

	@Override
	public final SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
		Packet me = build();
		for (EntityPlayer player : players) {
			checkNotClient(player).playerNetServerHandler.sendPacketToPlayer(me);
		}
		return this;
	}

	@Override
	public final SimplePacket sendToAll() {
		sendToList(Players.getAll());
		return this;
	}

	private void sendToList(List<EntityPlayerMP> players) {
		int len = players.size();
		Packet me = build();
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < len; ++i) {
			players.get(i).playerNetServerHandler.sendPacketToPlayer(me);
		}
	}

	private static RuntimeException clientPlayerExc() {
		return new IllegalArgumentException("Tried to send packet to client player");
	}

	@Override
	public final SimplePacket sendToAllInDimension(int dimension) {
		return sendToAllInDimension(DimensionManager.getWorld(dimension));
	}

	@Override
	public final SimplePacket sendToAllInDimension(World world) {
		sendToList(Players.allIn(checkNotClient(world)));
		return this;
	}

	@Override
	public final SimplePacket sendToAllNear(Entity entity, double radius) {
		return sendToAllNear(entity.worldObj, entity.posX, entity.posY, entity.posZ, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(TileEntity te, double radius) {
		return sendToAllNear(te.worldObj, te.xCoord, te.yCoord, te.zCoord, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(int dimension, double x, double y, double z, double radius) {
		return sendToAllNear(DimensionManager.getWorld(dimension), x, y, z, radius);
	}

	@Override
	public final SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
		List<EntityPlayerMP> players = Players.allIn(checkNotClient(world));
		int len = players.size();
		radius *= radius;

		Packet me = build();

		//noinspection ForLoopReplaceableByForEach
		for (int i = 0; i < len; ++i) {
			EntityPlayerMP player = players.get(i);
			double dx = x - player.posX;
			double dy = y - player.posY;
			double dz = z - player.posZ;
			if (dx * dx + dy * dy + dz * dz < radius) {
				player.playerNetServerHandler.sendPacketToPlayer(me);
			}
		}
		return this;
	}

	@Override
	public final SimplePacket sendToAllTracking(Entity entity) {
		checkNotClient(entity.worldObj).getEntityTracker().sendPacketToAllPlayersTrackingEntity(entity, build());
		return this;
	}

	@Override
	public final SimplePacket sendToAllAssociated(Entity entity) {
		checkNotClient(entity.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(entity, build());
		return this;
	}

	@Override
	public final SimplePacket sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
		PlayerInstance pi = checkNotClient(world).getPlayerManager().getOrCreateChunkWatcher(chunkX, chunkZ, false);
		if (pi != null) {
			pi.sendToAllPlayersWatchingChunk(build());
		}
		return this;
	}

	@Override
	public final SimplePacket sendToAllTracking(Chunk chunk) {
		return sendToAllTrackingChunk(chunk.worldObj, chunk.xPosition, chunk.zPosition);
	}

	@Override
	public final SimplePacket sendToAllTracking(TileEntity te) {
		return sendToAllTrackingChunk(te.worldObj, te.xCoord >> 4, te.zCoord >> 4);
	}

	@Override
	public final SimplePacket sendToViewing(Container c) {
		List<ICrafting> listeners = SCReflector.instance.getCrafters(c);
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = listeners.size(); i < len; ++i) {
			ICrafting listener = listeners.get(i);
			if (listener instanceof EntityPlayerMP) {
				((EntityPlayerMP) listener).playerNetServerHandler.sendPacketToPlayer(build());
				break;
			}
		}
		return this;
	}

	private static WorldServer checkNotClient(World world) {
		if (world.isRemote) {
			throw new IllegalArgumentException("Tried to send packet using a client world");
		}
		return (WorldServer) world;
	}

	private static EntityPlayerMP checkNotClient(EntityPlayer player) {
		if (player.worldObj.isRemote) {
			throw new IllegalArgumentException("Tried to send packet using a client player");
		}
		return (EntityPlayerMP) player;
	}
}
