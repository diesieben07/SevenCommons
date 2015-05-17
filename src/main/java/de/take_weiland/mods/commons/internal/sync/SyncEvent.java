package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.ProtocolException;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.Entities;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.internal.SCReflector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
public abstract class SyncEvent implements SyncCompanion.ChangeIterator {

    private static final int TILE_ENTITY = 0;
    private static final int ENTITY = 1;
    private static final int CONTAINER = 2;

    private int cursor = 0;
    ChangedValue<?>[] changes = new ChangedValue[3];

    public final void add(int fieldId, ChangedValue<?> changedValue) {
        changedValue.fieldId = fieldId;
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
        ChangedValue<?>[] newArr = new ChangedValue[currLen + 2];
        System.arraycopy(changes, 0, newArr, 0, currLen);
        changes = newArr;
    }

    abstract void writeMetaInfoToStream(MCDataOutput out);

    abstract SyncedObjectProxy getObjectDirect(EntityPlayer player);

    public final void writeTo(MCDataOutput out) {
        writeMetaInfoToStream(out);

        for (ChangedValue<?> change : changes) {
            out.writeVarInt(change.fieldId);
            change.writeData(out);
        }
        out.writeVarInt(0);
    }

    public final void applyDirect() {
        SyncedObjectProxy obj = getObjectDirect(Players.getClient());
        if (obj != null) {
            SyncCompanion companion = obj._sc$getCompanion();
            if (companion != null) {
                companion.applyChanges(obj, this);
            }
        }
    }

    public static void readAndApply(MCDataInput in) {
        EntityPlayer player = Players.getClient();
        int type = in.readByte();
        SyncedObjectProxy obj;
        switch (type) {
            case TILE_ENTITY:
                obj = ForTE.readObjectFromStream(player, in);
                break;
            case ENTITY:
                obj = ForEntity.readObjectFromStream(player, in);
                break;
            case CONTAINER:
                obj = ForContainer.readObjectFromStream(player, in);
                break;
            default:
                throw new ProtocolException("Invalid SyncType ID");
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public <T_DATA, T_OBJ, T_VAL> void apply(T_OBJ obj, Syncer<T_VAL, ?, T_DATA> syncer, Function<T_OBJ, T_VAL> getter, BiConsumer<T_OBJ, T_VAL> setter) {
        ChangedValue change = changes[cursor++];
        change.syncer.apply(change.data, obj, getter, setter);
    }

    public abstract void send(Object obj);

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

        static SyncedObjectProxy readObjectFromStream(EntityPlayer player, MCDataInput in) {
            int x = in.readInt();
            int y = in.readUnsignedByte();
            int z = in.readInt();

            return (SyncedObjectProxy) player.worldObj.getTileEntity(x, y, z);
        }

        @Override
        SyncedObjectProxy getObjectDirect(EntityPlayer player) {
            return (SyncedObjectProxy) player.worldObj.getTileEntity(x, y, z);
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
            SevenCommons.syncCodec.sendTo(this, Players.getTrackingChunk(te.getWorldObj(), te.xCoord >> 4, te.zCoord >> 4));
        }
    }

    public static final class ForEntity extends SyncEvent {

        private final int entityID;

        public ForEntity(Object obj) {
            this.entityID = ((Entity) obj).getEntityId();
        }

        static SyncedObjectProxy readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return (SyncedObjectProxy) player.worldObj.getEntityByID(in.readInt());
        }

        @Override
        SyncedObjectProxy getObjectDirect(EntityPlayer player) {
            return (SyncedObjectProxy) player.worldObj.getEntityByID(entityID);
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(ENTITY);
            out.writeInt(entityID);
        }

        @Override
        public void send(Object obj) {
            done();
            SevenCommons.syncCodec.sendTo(this, Entities.getTrackingPlayers((Entity) obj));
        }
    }

    public static final class ForContainer extends SyncEvent {

        private final int windowId;

        public ForContainer(Object obj) {
            this.windowId = ((Container) obj).windowId;
        }

        static SyncedObjectProxy readObjectFromStream(EntityPlayer player, MCDataInput in) {
            return player.openContainer.windowId == in.readByte() ? (SyncedObjectProxy) player.openContainer : null;
        }

        @Override
        SyncedObjectProxy getObjectDirect(EntityPlayer player) {
            return player.openContainer.windowId == windowId ? (SyncedObjectProxy) player.openContainer : null;
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
            SevenCommons.syncCodec.sendTo(this, Iterables.filter(crafters, EntityPlayer.class));
        }
    }

}
