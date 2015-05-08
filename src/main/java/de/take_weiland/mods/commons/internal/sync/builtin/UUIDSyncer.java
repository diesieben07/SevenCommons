package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;
import de.take_weiland.mods.commons.sync.SyncerConstructor;

import java.util.UUID;

/**
 * @author diesieben07
 */
final class UUIDSyncer extends AbstractSyncer.ForImmutable<UUID> {

    protected <OBJ> UUIDSyncer(OBJ obj, PropertyAccess<OBJ, UUID> property) {
        super(obj, property);
    }

    @Override
    protected UUID decode(MCDataInput in) {
        return in.readUUID();
    }

    @Override
    public void encode(UUID uuid, MCDataOutput out) {
        out.writeUUID(uuid);
    }

    public static void main(String[] args) {
        foo(UUIDSyncer::new);
    }

    static void foo(SyncerConstructor<UUID> constructor) {

    }
}
