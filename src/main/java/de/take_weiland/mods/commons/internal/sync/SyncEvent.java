package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.SchedulerInternalTask;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author diesieben07
 */
public abstract class SyncEvent extends SchedulerInternalTask implements SyncCompanion.ChangeIterator, BaseNettyPacket {

    public static final String CHANNEL = "SC|Sync";

    private static final int TILE_ENTITY = 0;
    private static final int ENTITY = 1;
    private static final int CONTAINER = 2;
    private static final int IEEP = 3;

    private int cursor = 0;
    ChangedValue<?>[] changes = new ChangedValue[3];

    public final void add(int fieldId, Syncer.Change<?> changedValue) {
        if (changes.length == cursor) {
            grow();
        }
        changes[cursor++] = changedValue;
        changedValue.fieldId = fieldId;
    }

    final void done() {
        cursor = 0;
    }

    private void grow() {
        int currLen = changes.length;
        ChangedValue<?>[] newArr = new ChangedValue[currLen + 2];
        System.arraycopy(changes, 0, newArr, 0, currLen);
        changes = newArr;
    }

    abstract void writeMetaInfoToStream(MCDataOutput out);

    abstract Object getObjectDirect(EntityPlayer player);


    public static void handle(byte[] payload) {
        MCDataInput in = Network.newInput(payload);

        EntityPlayer player = Players.getClient();

        int type = in.readByte();
        SyncedObjectProxy obj;
        try {
            switch (type) {
                case TILE_ENTITY:
                    obj = (SyncedObjectProxy) ForTE.readObjectFromStream(player, in);
                    break;
                case ENTITY:
                    obj = (SyncedObjectProxy) ForEntity.readObjectFromStream(player, in);
                    break;
                case CONTAINER:
                    obj = (SyncedObjectProxy) ForContainer.readObjectFromStream(player, in);
                    break;
                case IEEP:
                    obj = (SyncedObjectProxy) ForIEEP.readObjectFromStream(player, in);
                    break;
                default:
                    throw new ProtocolException("Invalid SyncType ID");
            }
        } catch (ClassCastException e) {
            obj = null;
        }
        if (obj != null) {
            SyncCompanion companion = obj._sc$getCompanion();
            if (companion != null) {
                /**
                 * MCDataInputImpl implements the ChangeIterator to avoid more object garbage
                 * @see MCDataInputImpl
                 */
                companion.applyChanges(obj, (SyncCompanion.ChangeIterator) in);
            }
        }
    }

    @Override
    public int fieldId() {
        if (cursor >= changes.length) {
            return SyncCompanion.FIELD_ID_END;
        }
        ChangedValue<?> change = changes[cursor];
        return change == null ? SyncCompanion.FIELD_ID_END : change.fieldId;
    }

    @Override
    public <T_DATA, T_VAL, T_COM> void apply(Syncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion) {
        @SuppressWarnings("unchecked")
        ChangedValue<T_DATA> change = (ChangedValue<T_DATA>) changes[cursor++];
        syncer.apply(change.data, obj, property, cObj, companion);
    }

    public void send(Object obj, @Nullable EntityPlayerMP player) {
        if (player == null) {
            send(obj);
        } else {
            done();
            NetworkImpl.sendRawPacket(player, this);
        }
    }

    abstract void send(Object obj);

    @Override
    public byte[] _sc$encode() {
        MCDataOutput out = Network.newOutput();

        writeMetaInfoToStream(out);

        for (ChangedValue<?> change : changes) {
            if (change == null) {
                break;
            }
            out.writeVarInt(change.fieldId);
            change.writeData(out);
        }
        out.writeVarInt(0);

        return out.toByteArray();
    }

    @Override
    public String _sc$channel() {
        return CHANNEL;
    }

    @Override
    public void _sc$handle(EntityPlayer player) {
        SchedulerInternalTask.add(Scheduler.client(), this);
    }

    @Override
    public byte _sc$characteristics() {
        return Network.CLIENT | Network.ASYNC;
    }

    @Override
    public boolean execute() {
        try {
            SyncedObjectProxy obj = (SyncedObjectProxy) getObjectDirect(Players.getClient());
            if (obj != null) {
                SyncCompanion companion = obj._sc$getCompanion();
                if (companion != null) {
                    companion.applyChanges(obj, this);
                }
            }
        } catch (ClassCastException ignored) {
            // the cast might fail, in that case we just ignore silently
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("SyncEvent (type=%s, %s)", type(), data());
    }


    abstract String type();

    abstract Object data();

    public static final class ForTE extends SyncEvent {

        private final int x;
        private final int y;
        private final int z;

        public ForTE(Object obj) {
            TileEntity te = (TileEntity) obj;
            this.x = te.xCoord;
            this.y = te.yCoord;
            this.z = te.zCoord;
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            int x = in.readInt();
            int y = in.readUnsignedByte();
            int z = in.readInt();

            return player.worldObj.getTileEntity(x, y, z);
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            return player.worldObj.getTileEntity(x, y, z);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(TILE_ENTITY);
            out.writeInt(x);
            out.writeByte(y);
            out.writeInt(z);
        }

        @Override
        public void send(Object obj) {
            done();
            TileEntity te = (TileEntity) obj;
            NetworkImpl.sendRawPacket(Players.getTrackingChunk(te.getWorld(), te.xCoord >> 4, te.zCoord >> 4), this);
        }

        @Override
        String type() {
            return "TileEntity";
        }

        @Override
        Object data() {
            return String.format("x=%s, y=%s, z=%s", x, y, z);
        }
    }

    public static final class ForEntity extends SyncEvent {

        private final int entityID;

        public ForEntity(Object obj) {
            this.entityID = ((Entity) obj).getEntityId();
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return player.worldObj.getEntityByID(in.readInt());
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            return player.worldObj.getEntityByID(entityID);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(ENTITY);
            out.writeInt(entityID);
        }

        @Override
        public void send(Object obj) {
            done();
            NetworkImpl.sendRawPacket(Entities.getTrackingPlayers((Entity) obj), this);
        }

        @Override
        String type() {
            return "Entity";
        }

        @Override
        Object data() {
            return String.format("id=%s", entityID);
        }
    }

    public static final class ForContainer extends SyncEvent {

        private final int windowId;

        public ForContainer(Object obj) {
            this.windowId = ((Container) obj).windowId;
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return player.openContainer.windowId == in.readByte() ? player.openContainer : null;
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            return player.openContainer.windowId == windowId ? player.openContainer : null;
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(CONTAINER);
            out.writeByte(windowId);
        }

        @Override
        public void send(Object obj) {
            done();
            Container container = (Container) obj;
            List<ICrafting> crafters = SCReflector.instance.getCrafters(container);
            NetworkImpl.sendRawPacket(Iterables.filter(crafters, EntityPlayerMP.class), this);
        }

        @Override
        String type() {
            return "Container";
        }

        @Override
        Object data() {
            return String.format("id=%s", windowId);
        }
    }

    public static final class ForIEEP extends SyncEvent {

        private final int entityID;
        private final String id;

        public ForIEEP(Object obj) {
            IEEPSyncCompanion companion = (IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion();
            entityID = companion._sc$entity.getEntityId();
            id = companion._sc$ident;
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            int entityID = in.readInt();
            String id = in.readString();
            Entity entity = player.worldObj.getEntityByID(entityID);
            if (entity == null) {
                return null;
            }
            return entity.getExtendedProperties(id);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(IEEP);
            out.writeInt(entityID);
            out.writeString(id);
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            Entity entity = player.worldObj.getEntityByID(entityID);
            if (entity == null) {
                return null;
            }
            return entity.getExtendedProperties(id);
        }

        @Override
        public void send(Object obj) {
            done();
            Entity entity = ((IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion())._sc$entity;
            NetworkImpl.sendRawPacket(Entities.getTrackingPlayers(entity), this);
            if (entity instanceof EntityPlayerMP) {
                NetworkImpl.sendRawPacket((EntityPlayerMP) entity, this);
            }
        }

        @Override
        String type() {
            return "EntityProperties";
        }

        @Override
        Object data() {
            return String.format("EntityID=%s, PropertyID=%s", entityID, id);
        }
    }
}
