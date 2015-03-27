package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class BooleanSyncer extends SyncerForImmutable<Boolean> {
    public BooleanSyncer() {
        super(boolean.class);
    }

    @Override
    public Boolean writeAndUpdate(Boolean value, Boolean companion, MCDataOutput out) {
        out.writeBoolean(value);
        return value;
    }

    @Override
    public Boolean read(Boolean oldValue, Boolean companion, MCDataInput in) {
        return in.readBoolean();
    }
}
