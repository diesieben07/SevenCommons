package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class LongSyncer extends SyncerForImmutable<Long> {

    public LongSyncer() {
        super(long.class);
    }

    @Override
    public Long writeAndUpdate(Long value, Long companion, MCDataOutput out) {
        out.writeLong(value);
        return value;
    }

    @Override
    public Long read(Long oldValue, Long companion, MCDataInput in) {
        return in.readLong();
    }
}
