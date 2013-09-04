package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.AbstractModPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.templates.SyncedTileEntity;

public class PacketTileEntitySync extends AbstractModPacket {

	private int x;
	private int y;
	private int z;
	private SyncedTileEntity te;
	private ByteArrayDataInput in;
	
	public <T extends TileEntity & SyncedTileEntity> PacketTileEntitySync(T te) {
		x = te.xCoord;
		y = te.yCoord;
		z = te.zCoord;
		this.te = te;
	}
	
	@Override
	protected void readData(byte[] data) {
		in = ByteStreams.newDataInput(data, 1); // don't want the packetId
		x = in.readInt();
		y = in.readInt();
		z = in.readInt();
	}

	@Override
	protected byte[] writeData() {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeByte(getType().getPacketId());
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(z);
		te.writeData(out);
		return out.toByteArray();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		TileEntity te = player.worldObj.getBlockTileEntity(x, y, z);
		if (te instanceof SyncedTileEntity) {
			((SyncedTileEntity)te).readData(in);
		}
	}

	@Override
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.TE_SYNC;
	}

}
