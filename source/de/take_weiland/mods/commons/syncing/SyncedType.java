package de.take_weiland.mods.commons.syncing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.take_weiland.mods.commons.network.AbstractModPacket;

interface SyncedType<T> {

	void outputInstanceInfo(T synced, ByteArrayDataOutput out);
	
	T restoreInstance(EntityPlayer clientPlayer, ByteArrayDataInput in);
	
	ByteArrayDataOutput provideDataOutput();
	
	AbstractModPacket providePacket(ByteArrayDataOutput out);
	
	static abstract class DefaultSyncedType<T> implements SyncedType<T> {

		@Override
		public ByteArrayDataOutput provideDataOutput() {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			PacketSync.addPacketId(out);
			writeTypeId(out);
			return out;
		}

		protected abstract void writeTypeId(ByteArrayDataOutput out);

		@Override
		public AbstractModPacket providePacket(ByteArrayDataOutput out) {
			return new PacketSync(out);
		}
		
	}
	
	static final SyncedType<Entity> ENTITY = new DefaultSyncedType<Entity>() {

		@Override
		public void outputInstanceInfo(Entity entity, ByteArrayDataOutput out) {
			out.writeInt(entity.entityId);
		}

		@Override
		public Entity restoreInstance(EntityPlayer clientPlayer, ByteArrayDataInput in) {
			return clientPlayer.worldObj.getEntityByID(in.readInt());
		}

		@Override
		protected void writeTypeId(ByteArrayDataOutput out) {
			out.writeByte(0);
		}
		
	};
	
	static final SyncedType<TileEntity> TILE_ENTITY = new DefaultSyncedType<TileEntity>() {

		@Override
		public void outputInstanceInfo(TileEntity te, ByteArrayDataOutput out) {
			out.writeInt(te.xCoord);
			out.writeInt(te.yCoord);
			out.writeInt(te.zCoord);
		}

		@Override
		public TileEntity restoreInstance(EntityPlayer clientPlayer, ByteArrayDataInput in) {
			int x = in.readInt();
			int y = in.readInt();
			int z = in.readInt();
			return clientPlayer.worldObj.getBlockTileEntity(x, y, z);
		}

		@Override
		protected void writeTypeId(ByteArrayDataOutput out) {
			out.writeByte(1);
		}
		
	};
	
	static final SyncedType<Container> CONTAINER = new DefaultSyncedType<Container>() {

		@Override
		public void outputInstanceInfo(Container container, ByteArrayDataOutput out) {
			out.writeByte(container.windowId);
		}

		@Override
		public Container restoreInstance(EntityPlayer clientPlayer, ByteArrayDataInput in) {
			int windowId = in.readUnsignedByte();
			Container container = clientPlayer.openContainer;
			return container.windowId == windowId ? container : null;
		}

		@Override
		protected void writeTypeId(ByteArrayDataOutput out) {
			out.writeByte(2);
		}
		
	};
	
	static final SyncedType<?>[] TYPES = new SyncedType<?>[] {
		ENTITY, TILE_ENTITY, CONTAINER
	};
	
}
