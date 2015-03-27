package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.net.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;

/**
 * @author diesieben07
 */
public final class SyncHelpers {

    public static MCDataOutput newOutStream(TileEntity te) {
        MCDataOutput out = newStream(SyncType.TILE_ENTITY);
        out.writeInt(te.xCoord);
        out.writeByte(te.yCoord);
        out.writeInt(te.zCoord);
        return out;
    }

    public static MCDataOutput newOutStream(Entity entity) {
        MCDataOutput out = newStream(SyncType.ENTITY);
        out.writeInt(entity.entityId);
        return out;
    }

    public static MCDataOutput newOutStream(Container container) {
        MCDataOutput out = newStream(SyncType.CONTAINER);
        out.writeByte(container.windowId);
        return out;
    }

    public static MCDataOutput newOutStream(IEEPSyncCompanion companion) {
        MCDataOutput out = newStream(SyncType.ENTITY_PROPS);
        out.writeInt(companion._sc$entity.entityId);
        out.writeVarInt(companion._sc$id);
        return out;
    }

    private static MCDataOutput newStream(SyncType syncType) {
        MCDataOutput out = SevenCommons.packets.createStream(SevenCommons.SYNC_PACKET_ID);
        out.writeEnum(syncType);
        return out;
    }

    public static void sendStream(TileEntity te, MCDataOutput out) {
        SevenCommons.packets.makePacket(out).sendToAllTracking(te);
    }

    public static void sendStream(Entity entity, MCDataOutput out) {
        SevenCommons.packets.makePacket(out).sendToAllAssociated(entity);
    }

    public static void sendStream(Container container, MCDataOutput out) {
        SevenCommons.packets.makePacket(out).sendToViewing(container);
    }

    static SyncType getSyncType(Class<?> clazz) {
        if (TileEntity.class.isAssignableFrom(clazz)) {
            return SyncType.TILE_ENTITY;
        } else if (Entity.class.isAssignableFrom(clazz)) {
            return SyncType.ENTITY;
        } else if (Container.class.isAssignableFrom(clazz)) {
            return SyncType.CONTAINER;
        } else if (IExtendedEntityProperties.class.isAssignableFrom(clazz)) {
            return SyncType.ENTITY_PROPS;
        } else {
            throw new IllegalArgumentException("@Sync in invalid class " + clazz);
        }
    }

}
