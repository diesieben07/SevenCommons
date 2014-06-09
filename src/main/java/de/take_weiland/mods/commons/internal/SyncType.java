package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.asm.MCPNames;
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

	ENTITY("entity", "net/minecraft/entity/Entity", MCPNames.F_WORLD_OBJ_ENTITY_MCP, MCPNames.F_WORLD_OBJ_ENTITY_SRG, MCPNames.M_ON_UPDATE_MCP, MCPNames.M_ON_UPDATE_SRG) {
		
		@Override
		void sendPacket(Object entity, SimplePacket p) {
			p.sendToAllTracking((Entity) entity);
		}

		@Override
		void injectInfo(Object obj, WritableDataBuf out) {
			out.writeInt(((Entity) obj).entityId);
		}

		@Override
		Object recreate(EntityPlayer player, DataBuf in) {
			return player.worldObj.getEntityByID(in.readInt());
		}
		
	},
	
	TILE_ENTITY("tileEntity", "net/minecraft/tileentity/TileEntity", MCPNames.F_WORLD_OBJ_TILEENTITY_MCP, MCPNames.F_WORLD_OBJ_TILEENTITY_SRG, MCPNames.M_UPDATE_ENTITY_MCP, MCPNames.M_UPDATE_ENTITY_SRG) {
		
		@Override
		void sendPacket(Object te, SimplePacket p) {
			p.sendToAllTracking((TileEntity) te);
		}

		@Override
		void injectInfo(Object obj, WritableDataBuf out) {
			TileEntity te = (TileEntity) obj;
			out.writeVarInt(te.xCoord);
			out.writeVarInt(te.yCoord);
			out.writeVarInt(te.zCoord);
		}

		@Override
		Object recreate(EntityPlayer player, DataBuf in) {
			int x = in.readVarInt();
			int y = in.readVarInt();
			int z = in.readVarInt();
			return player.worldObj.getBlockTileEntity(x, y, z);
		}
		
	},
	
	CONTAINER("container", null, null, null, MCPNames.M_DETECT_AND_SEND_CHANGES_MCP, MCPNames.M_DETECT_AND_SEND_CHANGES_SRG) {
		
		@Override
		void sendPacket(Object container, SimplePacket p) {
			p.sendToViewing((Container) container);
		}

		@Override
		void injectInfo(Object obj, WritableDataBuf out) {
			out.writeByte(((Container) obj).windowId);
		}

		@Override
		Object recreate(EntityPlayer player, DataBuf in) {
			return player.openContainer.windowId == in.readByte() ? player.openContainer : null;
		}
		
	},
	ENTITY_PROPS("entityProps", null, null, null, "_sc$tickEntityProps", "_sc$tickEntityProps") {
		
		@Override
		void sendPacket(Object props, SimplePacket p) {
			p.sendToAllAssociated(((SyncedEntityProperties) props)._sc$getPropsEntity());
		}

		@Override
		void injectInfo(Object obj, WritableDataBuf out) {
			SyncedEntityProperties sep = (SyncedEntityProperties) obj;
			ENTITY.injectInfo(sep._sc$getPropsEntity(), out);
			out.writeVarInt(sep._sc$getPropsIndex());
		}

		@Override
		Object recreate(EntityPlayer player, DataBuf in) {
			Object entity = ENTITY.recreate(player, in);
			if (entity != null) {
				List<SyncedEntityProperties> props = ((EntityProxy)entity)._sc$getSyncedProperties();
				if (props != null) {
					return JavaUtils.get(props, in.readVarInt());
				}
			}
			return null;
		}
		
	};

	private final String simpleName;
	private final String rootClass;
	private final String worldField;
	private final String worldFieldSrg;
	private final String tickMcp;
	private final String tickSrg;
	
	private SyncType(String simpleName, String rootClass, String worldField, String worldFieldSrg, String tickMcp, String tickSrg) {
		this.simpleName = simpleName;
		this.rootClass = rootClass;
		this.worldField = worldField;
		this.worldFieldSrg = worldFieldSrg;
		this.tickMcp = tickMcp;
		this.tickSrg = tickSrg;
	}

	String getRootClass() {
		return rootClass;
	}
	
	String getWorldFieldName() {
		return MCPNames.use() ? worldField : worldFieldSrg;
	}

	String getTickMethod() {
		return MCPNames.use() ? tickMcp : tickSrg;
	}

	public String getSimpleName() {
		return simpleName;
	}

	abstract void sendPacket(Object obj, SimplePacket p);
	abstract void injectInfo(Object obj, WritableDataBuf out);
	abstract Object recreate(EntityPlayer player, DataBuf in);

}
