package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public enum SyncType {

	ENTITY("net/minecraft/entity/Entity", MCPNames.F_WORLD_OBJ_ENTITY_MCP, MCPNames.F_WORLD_OBJ_ENTITY_SRG, MCPNames.M_ON_UPDATE_MCP, MCPNames.M_ON_UPDATE_SRG) {
		
		@Override
		public void sendPacket(Object entity, SimplePacket p) {
			p.sendToAllTracking((Entity) entity);
		}

		@Override
		public void injectInfo(Object obj, WritableDataBuf out) {
			out.putInt(((Entity)obj).entityId);
		}

		@Override
		public Object recreate(EntityPlayer player, DataBuf in) {
			return player.worldObj.getEntityByID(in.getInt());
		}
		
	},
	
	TILE_ENTITY("net/minecraft/tileentity/TileEntity", MCPNames.F_WORLD_OBJ_TILEENTITY_MCP, MCPNames.F_WORLD_OBJ_TILEENTITY_SRG, MCPNames.M_UPDATE_ENTITY_MCP, MCPNames.M_UPDATE_ENTITY_SRG) {
		
		@Override
		public void sendPacket(Object te, SimplePacket p) {
			p.sendToAllTracking((TileEntity) te);
		}

		@Override
		public void injectInfo(Object obj, WritableDataBuf out) {
			TileEntity te = (TileEntity) obj;
			out.putVarInt(te.xCoord);
			out.putVarInt(te.yCoord);
			out.putVarInt(te.zCoord);
		}

		@Override
		public Object recreate(EntityPlayer player, DataBuf in) {
			int x = in.getVarInt();
			int y = in.getVarInt();
			int z = in.getVarInt();
			return player.worldObj.getBlockTileEntity(x, y, z);
		}
		
	},
	
	CONTAINER(null, null, null, MCPNames.M_DETECT_AND_SEND_CHANGES_MCP, MCPNames.M_DETECT_AND_SEND_CHANGES_SRG) {
		
		@Override
		public void sendPacket(Object container, SimplePacket p) {
			p.sendToViewing((Container) container);
		}

		@Override
		public void injectInfo(Object obj, WritableDataBuf out) {
			out.putByte(((Container)obj).windowId);
		}

		@Override
		public Object recreate(EntityPlayer player, DataBuf in) {
			return player.openContainer.windowId == in.getByte() ? player.openContainer : null;
		}
		
	},
	ENTITY_PROPS(null, null, null, "_sc$tickEntityProps", "_sc$tickEntityProps") {
		
		@Override
		public void sendPacket(Object props, SimplePacket p) {
			p.sendToAllAssociated(((SyncedEntityProperties) props)._sc$getPropsEntity());
		}

		@Override
		public void injectInfo(Object obj, WritableDataBuf out) {
			SyncedEntityProperties sep = (SyncedEntityProperties) obj;
			ENTITY.injectInfo(sep._sc$getPropsEntity(), out);
			out.putVarInt(sep._sc$getPropsIndex());
		}

		@Override
		public Object recreate(EntityPlayer player, DataBuf in) {
			Object entity = ENTITY.recreate(player, in);
			if (entity != null) {
				List<SyncedEntityProperties> props = ((EntityProxy)entity)._sc$getSyncedProperties();
				if (props != null) {
					return JavaUtils.get(props, in.getVarInt());
				}
			}
			return null;
		}
		
	};
	
	private final String rootClass;
	private final String worldField;
	private final String worldFieldSrg;
	private final String tickMcp;
	private final String tickSrg;
	
	private SyncType(String rootClass, String worldField, String worldFieldSrg, String tickMcp, String tickSrg) {
		this.rootClass = rootClass;
		this.worldField = worldField;
		this.worldFieldSrg = worldFieldSrg;
		this.tickMcp = tickMcp;
		this.tickSrg = tickSrg;
	}

	public String getRootClass() {
		return rootClass;
	}
	
	public String getWorldFieldName() {
		return ASMUtils.useMcpNames() ? worldField : worldFieldSrg;
	}

	public String getTickMethod() {
		return ASMUtils.useMcpNames() ? tickMcp : tickSrg;
	}

	public abstract void sendPacket(Object obj, SimplePacket p);
	public abstract void injectInfo(Object obj, WritableDataBuf out);
	public abstract Object recreate(EntityPlayer player, DataBuf in);

}
