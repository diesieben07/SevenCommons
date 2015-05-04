package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.ImmutableList;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author diesieben07
 */
public abstract class SyncEvent<T_OBJ> {

    private static final int TILE_ENTITY = 0;
    private static final int ENTITY = 1;

    final List<ChangedValue<?>> changes;

    abstract void writeMetaInfoToStream(MCDataOutput out);

    abstract T_OBJ getClientsideObject(EntityPlayer player);

    SyncEvent(MCDataInput in) {
        changes = new ArrayList<>();
        int id = in.readVarInt();
        while (id != 0) {
            id =
        }
    }

    public final void writeTo(MCDataOutput out) {
        for (ChangedValue<?> change : changes) {
            out.writeVarInt(change.fieldId);
            change.write(out);
        }
        out.writeVarInt(0);
        writeMetaInfoToStream(out);
    }

    public final void apply(EntityPlayer player) {
        T_OBJ obj = getClientsideObject(player);
        SyncCompanion companion = ((SyncedObjectProxy) obj)._sc$getCompanion();
        for (ChangedValue<?> change : changes) {
            companion.applyChange(obj, change);
        }
    }

    public static void readApply(EntityPlayer player, MCDataInput in) {
        int type = in.readByte();
        switch (type) {
            case TILE_ENTITY:
                new ForTE(in).apply(player);
                break;
        }
    }

    public static final class ForTE extends SyncEvent<TileEntity> {

        private final int x;
        private final int y;
        private final int z;

        public ForTE(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        ForTE(MCDataInput in) {
            this.x = in.readInt();
            this.y = in.readUnsignedByte();
            this.z = in.readInt();
        }

        @Override
        void writeMetaInfoToStream(MCDataOutput out) {
            out.writeByte(TILE_ENTITY);
            out.writeInt(x);
            out.writeByte(y);
            out.writeInt(z);
        }

        @Override
        TileEntity getClientsideObject(EntityPlayer player) {
            return player.worldObj.getTileEntity(x, y, z);
        }
    }

}
