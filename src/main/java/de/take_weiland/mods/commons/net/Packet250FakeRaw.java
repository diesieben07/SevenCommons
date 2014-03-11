package de.take_weiland.mods.commons.net;

import cpw.mods.fml.common.network.PacketDispatcher;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.MiscUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author diesieben07
 */
class Packet250FakeRaw<TYPE extends Enum<TYPE>> extends Packet250CustomPayload implements SimplePacket {

	private static final int MAX_SINGLE_SIZE = 32766; // bug in Packet250CP doesn't allow 32767
	private final WritableDataBufImpl<TYPE> buf;
	private final FMLPacketHandlerImpl<TYPE> fmlPh;
	private final TYPE type;

	static {
		// we need to inject our fake packet class into the map so that getPacketId still works (can't override it)
		MiscUtil.getReflector().getClassToIdMap(null).put(Packet250FakeRaw.class, Integer.valueOf(250));
	}

	Packet250FakeRaw(WritableDataBufImpl<TYPE> buf, FMLPacketHandlerImpl<TYPE> fmlPh, TYPE type) {
		this.buf = buf;
		this.fmlPh = fmlPh;
		this.type = type;
	}

	@Override
	public void writePacketData(DataOutput out) {
		doWrite(out, type, fmlPh, buf);
	}

	@Override
	public void processPacket(NetHandler netHandler) {
		fmlPh.handle0(buf, type, netHandler.getPlayer());
	}

	@Override
	public final void readPacketData(DataInput in) {
		// we are never read!
		throw new AssertionError("Something went horribly wrong here!");
	}

	@Override
	public int getPacketSize() {
		return 0; // TODO is this needed?
	}

	void doWrite(DataOutput out, TYPE type, FMLPacketHandlerImpl<TYPE> fmlPacketHandler, WritableDataBufImpl<TYPE> buf) {
		int len = buf.available();
		try {
			if (len > MAX_SINGLE_SIZE - fmlPacketHandler.idSize.byteSize) {
				doWriteMulti(buf, out, len, fmlPacketHandler, type);
			} else {
				doWriteSingle(buf, out, fmlPacketHandler, type);
			}
		} catch (IOException e) {
			throw JavaUtils.throwUnchecked(e); // weird bug, can't declare IOException for some reason
		}
	}

	void writeHeader(FMLPacketHandlerImpl<TYPE> fmlPh, DataOutput out, int len) throws IOException {
		writeString(fmlPh.channel, out);
		out.writeShort(len + fmlPh.idSize.byteSize);
	}

	void doWriteSingle(WritableDataBufImpl<TYPE> buf, DataOutput out, FMLPacketHandlerImpl<TYPE> fmlPh, Enum type) throws IOException {
		writeHeader(fmlPh, out, buf.actualLen);

		fmlPh.idSize.write(out, type.ordinal(), false);
		out.write(buf.buf, 0, buf.actualLen);
	}

	void doWriteMulti(WritableDataBufImpl<TYPE> buf, DataOutput out, int len, FMLPacketHandlerImpl<TYPE> fmlPh, Enum type) throws IOException {
		FMLPacketHandlerImpl.IdSize idSize = fmlPh.idSize;
		int partSize = MAX_SINGLE_SIZE - idSize.byteSize - 2;
		int numParts = MathHelper.ceiling_float_int((float) len / (float) partSize);
		int packetId = type.ordinal();
		byte[] arr = buf.buf;
		for (int i = 0; i < numParts; ++i) {
			if (i != 0) {
				out.writeByte(250); // start a new Packet250 here
			}
			int offset = i * partSize;
			int thisPartLen = Math.min(partSize, len - offset);

			writeHeader(fmlPh, out, thisPartLen + 2);
			idSize.write(out, packetId, true);
			out.writeByte(i);
			out.writeByte(numParts);
			out.write(arr, offset, thisPartLen);
		}
	}

	// SimplePacket
	@Override
	public void sendTo(PacketTarget target) {
		target.send(this);
	}

	@Override
	public void sendToServer() {
		PacketDispatcher.sendPacketToServer(this);
	}

	@Override
	public void sendTo(EntityPlayer player) {
		Packets.sendPacketToPlayer(this, player);
	}

	@Override
	public void sendTo(Iterable<? extends EntityPlayer> players) {
		Packets.sendPacketToPlayers(this, players);
	}

	@Override
	public void sendToAll() {
		PacketDispatcher.sendPacketToAllPlayers(this);
	}

	@Override
	public void sendToAllInDimension(int dimension) {
		PacketDispatcher.sendPacketToAllInDimension(this, dimension);
	}

	@Override
	public void sendToAllInDimension(World world) {
		PacketDispatcher.sendPacketToAllInDimension(this, world.provider.dimensionId);
	}

	@Override
	public void sendToAllNear(World world, double x, double y, double z, double radius) {
		PacketDispatcher.sendPacketToAllAround(x, y, z, radius, world.provider.dimensionId, this);
	}

	@Override
	public void sendToAllNear(int dimension, double x, double y, double z, double radius) {
		PacketDispatcher.sendPacketToAllAround(x, y, z, radius, dimension, this);
	}

	@Override
	public void sendToAllNear(Entity entity, double radius) {
		PacketDispatcher.sendPacketToAllAround(entity.posX, entity.posY, entity.posZ, radius, entity.worldObj.provider.dimensionId, this);
	}

	@Override
	public void sendToAllNear(TileEntity te, double radius) {
		PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, radius, te.worldObj.provider.dimensionId, this);
	}

	@Override
	public void sendToAllTracking(Entity entity) {
		Packets.sendPacketToAllTracking(this, entity);
	}

	@Override
	public void sendToAllTracking(TileEntity te) {
		Packets.sendPacketToAllTracking(this, te);
	}

	@Override
	public void sendToAllAssociated(Entity e) {
		Packets.sendPacketToAllAssociated(this, e);
	}

	@Override
	public void sendToViewing(Container c) {
		Packets.sendPacketToViewing(this, c);
	}
}
