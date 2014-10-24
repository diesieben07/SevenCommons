package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author diesieben07
 */
public class Packet250FakeNoMP extends Packet250CustomPayload implements SimplePacket {

	private final PacketHandlerProxy handler;

	public Packet250FakeNoMP(PacketHandlerProxy handler, String channel, byte[] bytes, int len) {
		this.handler = handler;
		this.channel = channel;
		this.data = bytes;
		this.length = len;
	}

	@Override
	public void writePacketData(DataOutput out) {
		try {
			writeString(channel, out);
			ASMHooks.writeExtPacketLen(out, length);
			out.write(data, 0, length);
		} catch (IOException e) {
			// stupid bug
			throw JavaUtils.throwUnchecked(e);
		}
	}

	@Override
	public void processPacket(NetHandler nh) {
		handler.handlePacket(MCDataInputStream.create(data, 0, length), nh.getPlayer());
	}

	@Override
	public SimplePacket sendTo(PacketTarget target) {
		target.send(this);
		return this;
	}

	@Override
	public SimplePacket sendToServer() {
		Packets.sendToServer(this);
		return this;
	}

	@Override
	public SimplePacket sendTo(EntityPlayer player) {
		Packets.sendTo(this, player);
		return this;
	}

	@Override
	public SimplePacket sendTo(Iterable<? extends EntityPlayer> players) {
		Packets.sendTo(this, players);
		return this;
	}

	@Override
	public SimplePacket sendToAll() {
		Packets.sendToAll(this);
		return this;
	}

	@Override
	public SimplePacket sendToAllIn(World world) {
		Packets.sendToAllIn(this, world);
		return this;
	}

	@Override
	public SimplePacket sendToAllNear(World world, double x, double y, double z, double radius) {
		Packets.sendToAllNear(this, world, x, y, z, radius);
		return this;
	}

	@Override
	public SimplePacket sendToAllNear(Entity entity, double radius) {
		Packets.sendToAllNear(this, entity, radius);
		return this;
	}

	@Override
	public SimplePacket sendToAllNear(TileEntity te, double radius) {
		Packets.sendToAllNear(this, te, radius);
		return this;
	}

	@Override
	public SimplePacket sendToAllTracking(Entity entity) {
		Packets.sendToAllTracking(this, entity);
		return this;
	}

	@Override
	public SimplePacket sendToAllTracking(TileEntity te) {
		Packets.sendToAllTracking(this, te);
		return this;
	}

	@Override
	public SimplePacket sendToAllTrackingChunk(World world, int chunkX, int chunkZ) {
		Packets.sendToAllTrackingChunk(this, world, chunkX, chunkZ);
		return this;
	}

	@Override
	public SimplePacket sendToAllTracking(Chunk chunk) {
		Packets.sendToAllTracking(this, chunk);
		return this;
	}

	@Override
	public SimplePacket sendToAllAssociated(Entity e) {
		Packets.sendToAllAssociated(this, e);
		return this;
	}

	@Override
	public SimplePacket sendToViewing(Container c) {
		Packets.sendToViewing(this, c);
		return this;
	}
}
