package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.ModPacketProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.io.IOException;

/**
 * <p>An abstract base class for simpler Packet handling. Make a subclass of this for every type of Packet you have.</p>
 * <p>Register your packet classes with {@link de.take_weiland.mods.commons.net.Network#newChannel(String)}.</p>
 * <p>To send a packet, use the Methods implemented from {@link de.take_weiland.mods.commons.net.SimplePacket} like this:
 * <code><pre>
 *     new ExamplePacket("whatever").sendToServer();
 *     new OtherPacket(someObject).sendToPlayer(somePlayer).sendToPlayer(otherPlayer);
 * </pre></code></p>
 * <p>Add the {@link de.take_weiland.mods.commons.net.PacketDirection} annotation to your packet class to specify a valid
 * direction this packet can be send.</p>
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

	private Packet mcPacket;
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
		Packets.sendToServer(build());
		return this;
	}

	@Override
	public final SimplePacket sendTo(EntityPlayer player) {
		Packets.sendTo(build(), player);
		return this;
	}

	@Override
	public final SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
		Packets.sendTo(build(), players);
		return this;
	}

	@Override
	public final SimplePacket sendToAll() {
		Packets.sendToAll(build());
		return this;
	}

	@Override
	public final SimplePacket sendToAllIn(World world) {
		Packets.sendToAllIn(build(), world);
		return this;
	}

	@Override
	public final SimplePacket sendToAllNear(Entity entity, double radius) {
		Packets.sendToAllNear(build(), entity, radius);
		return this;
	}

	@Override
	public final SimplePacket sendToAllNear(TileEntity te, double radius) {
		Packets.sendToAllNear(build(), te, radius);
		return this;
	}

	@Override
	public final SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
		Packets.sendToAllNear(build(), world, x, y, z, radius);
		return this;
	}

	@Override
	public final SimplePacket sendToAllTracking(Entity entity) {
		Packets.sendToAllTracking(build(), entity);
		return this;
	}

	@Override
	public final SimplePacket sendToAllAssociated(Entity entity) {
		Packets.sendToAllAssociated(build(), entity);
		return this;
	}

	@Override
	public final SimplePacket sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
		Packets.sendToAllTrackingChunk(build(),  world, chunkX, chunkZ);
		return this;
	}

	@Override
	public final SimplePacket sendToAllTracking(Chunk chunk) {
		Packets.sendToAllTracking(build(), chunk);
		return this;
	}

	@Override
	public final SimplePacket sendToAllTracking(TileEntity te) {
		Packets.sendToAllTracking(build(), te);
		return this;
	}

	@Override
	public final SimplePacket sendToViewing(Container container) {
		Packets.sendToViewing(build(), container);
		return this;
	}

}
