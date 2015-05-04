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
    public Class<UUID> getCompanionType() {
        return UUID.class;
    }

    @Override
    public void write(UUID value, MCDataOutput out) {
        out.writeUUID(value);
    }

    @Override
    public UUID read(MCDataInput in) {
        return in.readUUID();
    }
}
