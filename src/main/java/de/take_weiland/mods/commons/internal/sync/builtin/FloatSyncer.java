package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class FloatSyncer extends SyncerForImmutable<Float> {
    public FloatSyncer() {
        super(float.class);
    }

    @Override
    public Float writeAndUpdate(Float value, Float companion, MCDataOutput out) {
        out.writeFloat(value);
        return value;
    }

    @Override
    public Float read(Float oldValue, Float companion, MCDataInput in) {
        return in.readFloat();
    }
}
