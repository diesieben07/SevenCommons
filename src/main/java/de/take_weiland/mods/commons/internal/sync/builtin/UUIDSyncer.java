package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

import java.util.UUID;

/**
 * @author diesieben07
 */
enum UUIDSyncer implements Syncer.ForImmutable<UUID> {
    INSTANCE;

    @Override
    public UUID decode(MCDataInput in) {
        return in.readUUID();
    }

    @Override
    public void encode(UUID uuid, MCDataOutput out) {
        out.writeUUID(uuid);
    }

    @Override
    public Class<UUID> companionType() {
        return UUID.class;
    }
}
