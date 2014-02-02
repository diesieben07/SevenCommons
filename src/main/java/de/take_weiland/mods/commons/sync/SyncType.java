package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataOutput;

import de.take_weiland.mods.commons.asm.ASMConstants;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.UnsignedShorts;

public enum SyncType {

	ENTITY("net/minecraft/entity/Entity", ASMConstants.F_WORLD_OBJ_ENTITY_MCP, ASMConstants.F_WORLD_OBJ_ENTITY_SRG) {
		
		@Override
		public void sendPacket(Object entity, Packet p) {
			Packets.sendPacketToAllTracking(p, (Entity) entity);
		}

		@Override
		public void injectInfo(Object obj, ByteArrayDataOutput out) {
			out.writeInt(((Entity)obj).entityId);
		}

		@Override
		public Object recreate(EntityPlayer player, DataInput in) throws IOException {
			return player.worldObj.getEntityByID(in.readInt());
		}
		
	},
	
	TILE_ENTITY("net/minecraft/tileentity/TileEntity", ASMConstants.F_WORLD_OBJ_TILEENTITY_MCP, ASMConstants.F_WORLD_OBJ_TILEENTITY_SRG) {
		
		@Override
		public void sendPacket(Object te, Packet p) {
			Packets.sendPacketToAllTracking(p, (TileEntity) te);
		}

		@Override
		public void injectInfo(Object obj, ByteArrayDataOutput out) {
			TileEntity te = (TileEntity) obj;
			out.writeInt(te.xCoord);
			out.writeInt(te.yCoord);
			out.writeInt(te.zCoord);
		}

		@Override
		public Object recreate(EntityPlayer player, DataInput in) throws IOException {
			int x = in.readInt();
			int y = in.readInt();
			int z = in.readInt();
			return player.worldObj.getBlockTileEntity(x, y, z);
		}
		
	},
	
	CONTAINER(null, null, null) {
		
		@Override
		public void sendPacket(Object container, Packet p) {
			Packets.sendPacketToViewing(p, (Container) container);
		}

		@Override
		public void injectInfo(Object obj, ByteArrayDataOutput out) {
			out.writeByte(((Container)obj).windowId);
		}

		@Override
		public Object recreate(EntityPlayer player, DataInput in) throws IOException {
			return player.openContainer.windowId == in.readByte() ? player.openContainer : null;
		}
		
	},
	ENTITY_PROPS(null, null, null) {
		
		@Override
		public void sendPacket(Object props, Packet p) {
			Packets.sendPacketToAllAssociated(p, ((SyncedEntityProperties) props)._sc_sync_getEntity());
		}

		@Override
		public void injectInfo(Object obj, ByteArrayDataOutput out) {
			SyncedEntityProperties sep = (SyncedEntityProperties) obj;
			ENTITY.injectInfo(sep._sc_sync_getEntity(), out);
			out.writeShort(UnsignedShorts.checkedCast(sep._sc_sync_getIndex()));
		}

		@Override
		public Object recreate(EntityPlayer player, DataInput in) throws IOException {
			Object entity = ENTITY.recreate(player, in);
			if (entity != null) {
				List<SyncedEntityProperties> props = ((EntityProxy)entity)._sc_sync_getSyncedProperties();
				if (props != null) {
					return JavaUtils.safeListAccess(props, in.readUnsignedShort());
				}
			}
			return null;
		}
		
	};
	
	private final String rootClass;
	private final String worldField;
	private final String worldFieldSrg;
	
	private SyncType(String rootClass, String worldField, String worldFieldSrg) {
		this.rootClass = rootClass;
		this.worldField = worldField;
		this.worldFieldSrg = worldFieldSrg;
	}

	public String getRootClass() {
		return rootClass;
	}
	
	public String getWorldFieldName() {
		return ASMUtils.useMcpNames() ? worldField : worldFieldSrg;
	}

	public abstract void sendPacket(Object obj, Packet p);
	public abstract void injectInfo(Object obj, ByteArrayDataOutput out);
	public abstract Object recreate(EntityPlayer player, DataInput in) throws IOException;

}
