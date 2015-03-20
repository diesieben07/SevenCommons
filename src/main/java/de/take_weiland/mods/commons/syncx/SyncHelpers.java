package de.take_weiland.mods.commons.syncx;

import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.net.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

/**
 * @author diesieben07
 */
final class SyncHelpers {

    static MCDataOutput newOutStream(SyncType type, Object object) {
        MCDataOutput out = SevenCommons.packets.createStream(SevenCommons.SYNC_PACKET_ID);
        out.writeEnum(type);
        type.writeObject(object, out);
        return out;
    }

    static void sendStream(SyncType type, Object object, MCDataOutput out) {
        type.sendPacket(object, SevenCommons.packets.makePacket(out));
    }

    static SyncType getSyncType(Class<?> clazz) {
        if (TileEntity.class.isAssignableFrom(clazz)) {
            return SyncType.TILE_ENTITY;
        } else if (Entity.class.isAssignableFrom(clazz)) {
            return SyncType.ENTITY;
        } else if (Container.class.isAssignableFrom(clazz)) {
            return SyncType.CONTAINER;
        } else {
            // TODO
            throw new RuntimeException("TODO");
        }
    }

}
