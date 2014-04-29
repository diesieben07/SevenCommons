package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.List;

public final class PacketEntityPropsIds extends ModPacket {

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
			out.putString(props.get(i)._sc$getPropsIdentifier());
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
			((EntityProxy)e)._sc$setSyncedProperties(Arrays.asList(props));
		}
	}
	
	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

}
