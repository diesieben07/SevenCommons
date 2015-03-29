package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

import java.util.UUID;

/**
 * @author diesieben07
 */
class UUIDSyncer extends SyncerDefaultEquals<UUID> {
    public UUIDSyncer() {
        super(UUID.class);
    }

    @Override
    public UUID writeAndUpdate(UUID value, UUID companion, MCDataOutput out) {
        out.writeUUID(value);
        return value;
    }

    @Override
    public UUID read(UUID oldValue, UUID companion, MCDataInput in) {
        return in.readUUID();
    }
}
