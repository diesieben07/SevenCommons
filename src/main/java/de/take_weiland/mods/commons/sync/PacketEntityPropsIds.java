package de.take_weiland.mods.commons.sync;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.Internal;
import de.take_weiland.mods.commons.internal.CommonsPackets;
import de.take_weiland.mods.commons.internal.SCPacket;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
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

@Internal
public final class PacketEntityPropsIds extends SCPacket {

	private Entity entity;
	private List<SyncedEntityProperties> props;
	
	public PacketEntityPropsIds(Entity entity, List<SyncedEntityProperties> props) {
		this.entity = entity;
		this.props = props;
	}

	@Override
	protected void write(WritableDataBuf out) {
		out.putInt(entity.entityId);
		int len = props.size();
		out.putVarInt(len);
		for (int i = 0; i < len; ++i) {
			out.putString(props.get(i)._sc_sync_getIdentifier());
		}
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		int entityId = in.getInt();
		Entity e = player.worldObj.getEntityByID(entityId);
		if (e != null) {
			int len = in.getVarInt();
			SyncedEntityProperties[] props = new SyncedEntityProperties[len];
			for (int i = 0; i < len; ++i) {
				props[i] = (SyncedEntityProperties) e.getExtendedProperties(in.getString());
			}
			((EntityProxy)e)._sc_sync_setSyncedProperties(Arrays.asList(props));
			System.out.println("received properties: " + ((EntityProxy)e)._sc_sync_getSyncedProperties());
		}
	}
	
	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

}
