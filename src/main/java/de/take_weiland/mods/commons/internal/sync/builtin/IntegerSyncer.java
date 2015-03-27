package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class IntegerSyncer extends SyncerForImmutable<Integer> {

    public IntegerSyncer() {
        super(int.class);
    }

    @Override
    public Integer writeAndUpdate(Integer value, Integer companion, MCDataOutput out) {
        out.writeInt(value);
        return value;
    }

    @Override
    public Integer read(Integer oldValue, Integer companion, MCDataInput in) {
        return in.readInt();
    }
}
