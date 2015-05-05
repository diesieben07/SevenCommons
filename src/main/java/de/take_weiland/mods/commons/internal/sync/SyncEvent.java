package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.ProtocolException;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

/**
 * @author diesieben07
 */
public abstract class SyncEvent implements SyncCompanion.ChangeIterator {

    private static final int TILE_ENTITY = 0;
    private static final int ENTITY = 1;
    private static final int CONTAINER = 2;

    private int cursor = 0;
    final List<ChangedValue<?>> changes;

    SyncEvent(List<ChangedValue<?>> changes) {
        this.changes = changes;
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

    public final void applyDirect(EntityPlayer player) {
        SyncedObjectProxy obj = getObjectDirect(player);
        if (obj != null) {
            SyncCompanion companion = obj._sc$getCompanion();
            if (companion != null) {
                companion.applyChanges(obj, this);
            }
        }
    }

    public static void readAndApply(EntityPlayer player, MCDataInput in) {
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
        return cursor < changes.size() ? changes.get(cursor).fieldId : SyncCompanion.FIELD_ID_END;
    }

    @Override
    public <T_DATA> T_DATA value(Syncer<?, T_DATA, ?> syncer) {
        //noinspection unchecked
        return (T_DATA) changes.get(cursor++).data;
    }

    public static final class ForTE extends SyncEvent {

        private final int x;
        private final int y;
        private final int z;

        public ForTE(int x, int y, int z, List<ChangedValue<?>> changes) {
            super(changes);
            this.x = x;
            this.y = y;
            this.z = z;
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
    }

    public static final class ForEntity extends SyncEvent {

        private final int entityID;

        public ForEntity(int entityID, List<ChangedValue<?>> changes) {
            super(changes);
            this.entityID = entityID;
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
    }

    public static final class ForContainer extends SyncEvent {

        private final int windowId;

        public ForContainer(int windowId, List<ChangedValue<?>> changes) {
            super(changes);
            this.windowId = windowId;
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
    }

}
