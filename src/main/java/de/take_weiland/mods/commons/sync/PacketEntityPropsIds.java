package de.take_weiland.mods.commons.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PacketEntityPropsIds extends DataPacket {

	private Entity entity;
	private List<SyncedEntityProperties> props;
	
	public PacketEntityPropsIds(Entity entity, List<SyncedEntityProperties> props) {
		this.entity = entity;
		this.props = props;
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		out.writeInt(entity.entityId);
		int len = props.size();
		out.writeShort(UnsignedShorts.checkedCast(len));
		for (int i = 0; i < len; ++i) {
			out.writeUTF(props.get(i)._sc_sync_getIdentifier());
		}
	}

	static final Integer MAX = Integer.valueOf(Integer.MAX_VALUE);
	
	@Override
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		int entityId = in.readInt();
		Entity e = player.worldObj.getEntityByID(entityId);
		if (e != null) {
			int len = in.readUnsignedShort();
			SyncedEntityProperties[] props = new SyncedEntityProperties[len];
			for (int i = 0; i < len; ++i) {
				props[i] = (SyncedEntityProperties) e.getExtendedProperties(in.readUTF());
			}
			((EntityProxy)e)._sc_sync_setSyncedProperties(Arrays.asList(props));
			System.out.println("received properties: " + ((EntityProxy)e)._sc_sync_getSyncedProperties());
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
		return CommonsPackets.SYNC_ENTITY_PROPS_IDS;
	}

}
