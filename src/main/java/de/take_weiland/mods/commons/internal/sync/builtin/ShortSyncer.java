package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class ShortSyncer extends SyncerForImmutable<Short> {
    public ShortSyncer() {
        super(short.class);
    }

    @Override
    public Short writeAndUpdate(Short value, Short companion, MCDataOutput out) {
        out.writeShort(value);
        return value;
    }

    @Override
    public Short read(Short oldValue, Short companion, MCDataInput in) {
        return in.readShort();
    }
}
