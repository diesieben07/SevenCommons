package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.internal.SyncedEntityProperties;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

/**
 * @author diesieben07
 */
public enum SyncType {

	TILE_ENTITY {
        @Override
		public void writeObject(Object object, MCDataOutput out) {
			TileEntity te = (TileEntity) object;
			out.writeInt(te.xCoord);
			out.writeByte(te.yCoord);
			out.writeInt(te.zCoord);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			return player.worldObj.getBlockTileEntity(in.readInt(), in.readUnsignedByte(), in.readInt());
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllTracking((TileEntity) object);
		}
	},
	ENTITY {
        @Override
		public void writeObject(Object object, MCDataOutput out) {
			out.writeInt(((Entity) object).entityId);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			return player.worldObj.getEntityByID(in.readInt());
		}

        @Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllTracking((Entity) object);
		}
	},
	CONTAINER {
        @Override
		public void writeObject(Object object, MCDataOutput out) {
			out.writeByte(((Container) object).windowId);
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			Container container = player.openContainer;
			return container.windowId == in.readByte() ? container : null;
		}

        @Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToViewing((Container) object);
		}
	},
	ENTITY_PROPS {
        @Override
		public void writeObject(Object object, MCDataOutput out) {
			SyncedEntityProperties props = (SyncedEntityProperties) object;
			out.writeInt(props._sc$syncprops$owner().entityId);
			out.writeInt(props._sc$syncprops$index());
		}

		@Override
		public void sendPacket(Object object, SimplePacket packet) {
			packet.sendToAllAssociated(((SyncedEntityProperties) object)._sc$syncprops$owner());
		}

		@Override
		public Object readObject(EntityPlayer player, MCDataInputStream in) {
			int entityId = in.readInt();
			int propsId = in.readInt();

			Entity entity = player.worldObj.getEntityByID(entityId);
			if (entity == null) {
				return null;
			}
			List<SyncedEntityProperties> props = ((EntityProxy) entity)._sc$getSyncedProps();
			if (props == null) {
				return null;
			}
			return JavaUtils.get(props, propsId);
		}

    };

    public abstract void writeObject(Object object, MCDataOutput out);

	public abstract void sendPacket(Object object, SimplePacket packet);

	public abstract Object readObject(EntityPlayer player, MCDataInputStream in);

}
