package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.internal.CommonMethodHandles;
import de.take_weiland.mods.commons.internal.net.InternalPacket;
import de.take_weiland.mods.commons.internal.net.MCDataInputImpl;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.sync_olds.*;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.ProtocolException;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.util.Scheduler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author diesieben07
 */
public abstract class SyncEvent implements SyncCompanion.ChangeIterator, InternalPacket, Scheduler.Task {

    public static final String CHANNEL = "SC|Sync";

    private static final int TILE_ENTITY = 0;
    private static final int ENTITY = 1;
    private static final int CONTAINER = 2;
    private static final int IEEP = 3;

    private int cursor = 0;
    ChangedValue[] changes = new ChangedValue[3];

    public final void add(ChangedValue changedValue) {
        if (changes.length == cursor) {
            grow();
        }
        changes[cursor++] = changedValue;
    }

    final void done() {
        cursor = 0;
    }

    private void grow() {
        int currLen = changes.length;
        ChangedValue[] newArr = new ChangedValue[currLen + 2];
        System.arraycopy(changes, 0, newArr, 0, currLen);
        changes = newArr;
    }

    abstract void writeMetaInfoToStream(MCDataOutput out);

    abstract Object getObjectDirect(EntityPlayer player);

    public static void handle(ByteBuf payload) {
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
//                case IEEP:
//                    obj = (SyncedObjectProxy) ForIEEP.readObjectFromStream(player, in);
//                    break;
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
    public int nextFieldId() {
        if (cursor >= changes.length) {
            return SyncCompanion.FIELD_ID_END;
        }
        ChangedValue change = changes[cursor];
        return change == null ? SyncCompanion.FIELD_ID_END : change.fieldId;
    }

    @Override
    public <T_DATA, T_VAL, T_COM> void apply(TypeSyncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion) {
        // TODO
        ChangedValue change = changes[cursor++];
//        syncer.apply(change.data, obj, property, cObj, companion);
    }

    public void send(Object obj, @Nullable EntityPlayerMP player) {
        if (player == null) {
            send(obj);
        } else {
            done();
            NetworkImpl.sendPacket(this, player);
        }
    }

    abstract void send(Object obj);

    @Override
    public void _sc$internal$writeTo(MCDataOutput out) throws Exception {
        writeMetaInfoToStream(out);

        for (ChangedValue change : changes) {
            if (change == null) {
                break;
            }
            out.writeVarInt(change.fieldId);
            change.writeData(out);
        }
        out.writeVarInt(0);
    }

    @Override
    public String _sc$internal$channel() {
        return CHANNEL;
    }

    @Override
    public int _sc$internal$expectedSize() {
        return 32;
    }

    @Override
    public void _sc$internal$receiveDirect(byte side, NetworkManager manager) {
        Scheduler.client().execute(this);
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

        private final BlockPos pos;

        public ForTE(Object obj) {
            this.pos = ((TileEntity) obj).getPos().toImmutable();
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return player.world.getTileEntity(in.readBlockPos());
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            return player.world.getTileEntity(pos);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(TILE_ENTITY);
            out.writeBlockPos(pos);
        }

        @Override
        public void send(Object obj) {
            done();
            TileEntity te = (TileEntity) obj;
            BlockPos pos = te.getPos();
            for (EntityPlayerMP player : Players.getTrackingChunk(te.getWorld(), pos.getX() >> 4, pos.getZ() >> 4)) {
                NetworkImpl.sendPacket(this, player);
            }
        }

        @Override
        String type() {
            return "TileEntity";
        }

        @Override
        Object data() {
            return String.format("pos=%s", pos);
        }
    }

    public static final class ForEntity extends SyncEvent {

        private final int entityID;

        public ForEntity(Object obj) {
            this.entityID = ((Entity) obj).getEntityId();
        }

        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return player.world.getEntityByID(in.readInt());
        }

        @Override
        Object getObjectDirect(EntityPlayer player) {
            return player.world.getEntityByID(entityID);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(ENTITY);
            out.writeInt(entityID);
        }

        @Override
        public void send(Object obj) {
            done();
            for (EntityPlayerMP player : Entities.getTrackingPlayers((Entity) obj)) {
                NetworkImpl.sendPacket(this, player);
            }
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
            List<IContainerListener> listeners = CommonMethodHandles.getListeners(container);
            for (IContainerListener listener : listeners) {
                if (listener instanceof EntityPlayerMP) {
                    NetworkImpl.sendPacket(this, (EntityPlayerMP) listener);
                }
            }
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

//    public static final class ForIEEP extends SyncEvent {
//
//        private final int entityID;
//        private final String id;
//
//        public ForIEEP(Object obj) {
//            IEEPSyncCompanion companion = (IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion();
//            entityID = companion._sc$entity.getEntityId();
//            id = companion._sc$ident;
//        }
//
//        static Object readObjectFromStream(EntityPlayer player, MCDataInput in) {
//            int entityID = in.readInt();
//            String id = in.readString();
//            Entity entity = player.worldObj.getEntityByID(entityID);
//            if (entity == null) {
//                return null;
//            }
//            return entity.getExtendedProperties(id);
//        }
//
//        @Override
//        void writeMetaInfoToStream(MCDataOutput out) {
//            out.writeByte(IEEP);
//            out.writeInt(entityID);
//            out.writeString(id);
//        }
//
//        @Override
//        Object getObjectDirect(EntityPlayer player) {
//            Entity entity = player.worldObj.getEntityByID(entityID);
//            if (entity == null) {
//                return null;
//            }
//            return entity.getExtendedProperties(id);
//        }
//
//        @Override
//        public void send(Object obj) {
//            done();
//            Entity entity = ((IEEPSyncCompanion) ((SyncedObjectProxy) obj)._sc$getCompanion())._sc$entity;
//            NetworkImpl.sendRawPacket(Entities.getTrackingPlayers(entity), this);
//            if (entity instanceof EntityPlayerMP) {
//                NetworkImpl.sendRawPacket((EntityPlayerMP) entity, this);
//            }
//        }
//
//        @Override
//        String type() {
//            return "EntityProperties";
//        }
//
//        @Override
//        Object data() {
//            return String.format("EntityID=%s, PropertyID=%s", entityID, id);
//        }
//    }
}
