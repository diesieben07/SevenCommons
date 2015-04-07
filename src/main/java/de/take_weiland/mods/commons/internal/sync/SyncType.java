package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.internal.EntityProxy;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.List;

/**
 * @author diesieben07
 */
enum SyncType {

	TILE_ENTITY {
        @Override
        public boolean doRead(EntityPlayer player, MCDataInput in) {
            SyncedObjectProxy te = (SyncedObjectProxy) player.worldObj.getBlockTileEntity(in.readInt(), in.readUnsignedByte(), in.readInt());
            if (te == null) {
                return false;
            }
            SyncCompanion companion = te._sc$getCompanion();
            if (companion == null) {
                return false;
            }
            companion.read(te, in);
            return true;
        }

    },
	ENTITY {
        @Override
        public boolean doRead(EntityPlayer player, MCDataInput in) {
            SyncedObjectProxy entity = (SyncedObjectProxy) player.worldObj.getEntityByID(in.readInt());
            if (entity == null) {
                return false;
            }
            SyncCompanion companion = entity._sc$getCompanion();
            if (companion == null) {
                return false;
            }
            companion.read(entity, in);
            return true;
        }

    },
	CONTAINER {
        @Override
        public boolean doRead(EntityPlayer player, MCDataInput in) {
            Container container = player.openContainer;
            SyncCompanion companion = ((SyncedObjectProxy) container)._sc$getCompanion();
            if (container.windowId != in.readByte() || companion == null) {
                return false;
            }
            companion.read(container, in);
            return true;
        }

    },
	ENTITY_PROPS {
        @Override
        public boolean doRead(EntityPlayer player, MCDataInput in) {
            int entityId = in.readInt();
            int propsId = in.readVarInt();

            Entity entity = player.worldObj.getEntityByID(entityId);
            if (entity == null) {
                return false;
            }
            List<IEEPSyncCompanion> props = ((EntityProxy) entity)._sc$getPropsCompanions();
            if (props == null) {
                return false;
            }
            IEEPSyncCompanion companion = JavaUtils.get(props, propsId);
            companion.read(companion._sc$ieep, in);
            return true;
        }
    };

    public abstract boolean doRead(EntityPlayer player, MCDataInput in);
}
