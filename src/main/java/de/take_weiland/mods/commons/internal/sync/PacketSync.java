package de.take_weiland.mods.commons.internal.sync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.Packets;

public class PacketSync extends DataPacket {

	private SyncedObject obj;
	private SyncType type;
	
	public PacketSync(SyncedObject obj, SyncType type) {
		this.obj = obj;
		this.type = type;
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		Packets.writeEnum(out, type);
		switch (type) {
		case CONTAINER:
			out.writeByte(((Container) obj).windowId);
			break;
		case ENTITY:
			out.writeInt(((Entity) obj).entityId);
			break;
		case TILE_ENTITY:
			TileEntity te = (TileEntity) obj;
			out.writeInt(te.xCoord);
			out.writeInt(te.yCoord);
			out.writeInt(te.zCoord);
			break;
		}
		obj._SC_SYNC_write(out);
	}

	@Override
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		type = Packets.readEnum(in, SyncType.class);
		Object readObj = null;
		switch (type) {
		case CONTAINER:
			int windowId = in.readByte();
			if (player.openContainer.windowId == windowId) {
				readObj = player.openContainer;
			}
			break;
		case ENTITY:
			readObj = player.worldObj.getEntityByID(in.readInt());
			break;
		case TILE_ENTITY:
			int x = in.readInt();
			int y = in.readInt();
			int z = in.readInt();
			readObj = player.worldObj.getBlockTileEntity(x, y, z);
			break;
		}
		if (readObj instanceof SyncedObject) {
			((SyncedObject) readObj)._SC_SYNC_read(in);
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) { }

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.SYNC;
	}
}
