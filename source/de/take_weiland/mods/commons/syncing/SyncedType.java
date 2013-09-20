package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import de.take_weiland.mods.commons.internal.CommonsModContainer;
import de.take_weiland.mods.commons.network.ModPacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

interface SyncedType<T> {

	void outputInstanceInfo(T synced, MinecraftDataOutput out);
	
	T restoreInstance(EntityPlayer clientPlayer, MinecraftDataInput in);
	
	MinecraftDataOutput provideDataOutput();
	
	ModPacket providePacket(MinecraftDataOutput out);
	
	static abstract class DefaultSyncedType<T> implements SyncedType<T> {

		@Override
		public MinecraftDataOutput provideDataOutput() {
			MinecraftDataOutput out = MinecraftDataOutput.create();
			out.skip(CommonsModContainer.packetTransport.bytePrefixCount());
			writeTypeId(out);
			return out;
		}

		protected abstract void writeTypeId(MinecraftDataOutput out);

		@Override
		public ModPacket providePacket(MinecraftDataOutput out) {
			return new PacketSync(out);
		}
		
	}
	
	static final SyncedType<Entity> ENTITY = new DefaultSyncedType<Entity>() {

		@Override
		public void outputInstanceInfo(Entity entity, MinecraftDataOutput out) {
			out.writeInt(entity.entityId);
		}

		@Override
		public Entity restoreInstance(EntityPlayer clientPlayer, MinecraftDataInput in) {
			return clientPlayer.worldObj.getEntityByID(in.readInt());
		}

		@Override
		protected void writeTypeId(MinecraftDataOutput out) {
			out.writeByte(0);
		}
		
	};
	
	static final SyncedType<TileEntity> TILE_ENTITY = new DefaultSyncedType<TileEntity>() {

		@Override
		public void outputInstanceInfo(TileEntity te, MinecraftDataOutput out) {
			out.writeInt(te.xCoord);
			out.writeInt(te.yCoord);
			out.writeInt(te.zCoord);
		}

		@Override
		public TileEntity restoreInstance(EntityPlayer clientPlayer, MinecraftDataInput in) {
			int x = in.readInt();
			int y = in.readInt();
			int z = in.readInt();
			return clientPlayer.worldObj.getBlockTileEntity(x, y, z);
		}

		@Override
		protected void writeTypeId(MinecraftDataOutput out) {
			out.writeByte(1);
		}
		
	};
	
	static final SyncedType<Container> CONTAINER = new DefaultSyncedType<Container>() {

		@Override
		public void outputInstanceInfo(Container container, MinecraftDataOutput out) {
			out.writeByte(container.windowId);
		}

		@Override
		public Container restoreInstance(EntityPlayer clientPlayer, MinecraftDataInput in) {
			int windowId = in.readUnsignedByte();
			Container container = clientPlayer.openContainer;
			return container.windowId == windowId ? container : null;
		}

		@Override
		protected void writeTypeId(MinecraftDataOutput out) {
			out.writeByte(2);
		}
		
	};
	
	static final SyncedType<?>[] TYPES = new SyncedType<?>[] {
		ENTITY, TILE_ENTITY, CONTAINER
	};
	
}
