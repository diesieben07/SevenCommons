package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class DoubleSyncer extends SyncerForImmutable<Double> {
    public DoubleSyncer() {
        super(double.class);
    }

    @Override
    public Double writeAndUpdate(Double value, Double companion, MCDataOutput out) {
        out.writeDouble(value);
        return value;
    }

    @Override
    public Double read(Double oldValue, Double companion, MCDataInput in) {
        return in.readDouble();
    }
}
