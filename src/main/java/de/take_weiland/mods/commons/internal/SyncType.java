package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;

public enum SyncType {

	ENTITY("entity", "net/minecraft/entity/Entity", MCPNames.F_WORLD_OBJ_ENTITY, MCPNames.M_ON_UPDATE) {
		
		@Override
		public void sendPacket(Object entity, SimplePacket p) {
			p.sendToAllAssociated((Entity) entity);
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
	
	TILE_ENTITY("tileEntity", "net/minecraft/tileentity/TileEntity", MCPNames.F_WORLD_OBJ_TILEENTITY, MCPNames.M_UPDATE_ENTITY) {
		
		@Override
		public void sendPacket(Object te, SimplePacket p) {
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
	
	CONTAINER("container", null, null, MCPNames.M_DETECT_AND_SEND_CHANGES) {
		
		@Override
		public void sendPacket(Object container, SimplePacket p) {
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
	ENTITY_PROPS("entityProps", null, null, SyncedEntityProperties.TICK) {
		
		@Override
		public void sendPacket(Object props, SimplePacket p) {
			p.sendToAllAssociated(((SyncedEntityProperties) props)._sc$getPropsEntity());
		}

		@Override
		void injectInfo(Object obj, WritableDataBuf out) {
			SyncedEntityProperties sep = (SyncedEntityProperties) obj;
			ENTITY.injectInfo(sep._sc$getPropsEntity(), out);
			out.writeString(sep._sc$getPropsIdentifier());
		}

		@Override
		Object recreate(EntityPlayer player, DataBuf in) {
			Entity entity = (Entity) ENTITY.recreate(player, in);
			if (entity != null) {
				IExtendedEntityProperties props = entity.getExtendedProperties(in.readString());
				return props instanceof SyncedEntityProperties ? props : null;
			}
			return null;
		}
		
	};
	public static final String CLASS_TILE_ENTITY = "net/minecraft/tileentity/TileEntity";
	public static final String CLASS_ENTITY = "net/minecraft/entity/Entity";
	public static final String CLASS_CONTAINER = "net/minecraft/inventory/Container";

	private final String simpleName;
	private final String rootClass;
	private final String worldFieldSrg;
	private final String tickSrg;
	
	private SyncType(String simpleName, String rootClass, String worldFieldSrg, String tickSrg) {
		this.simpleName = simpleName;
		this.rootClass = rootClass;
		this.worldFieldSrg = worldFieldSrg;
		this.tickSrg = tickSrg;
	}

	public String getRootClass() {
		return rootClass;
	}

	public String getWorldFieldName() {
		return MCPNames.field(worldFieldSrg);
	}

	public String getTickMethod() {
		return this == ENTITY_PROPS ? tickSrg : MCPNames.method(tickSrg);
	}

	public String getSimpleName() {
		return simpleName;
	}

	public abstract void sendPacket(Object obj, SimplePacket p);
	abstract void injectInfo(Object obj, WritableDataBuf out);
	abstract Object recreate(EntityPlayer player, DataBuf in);

}
