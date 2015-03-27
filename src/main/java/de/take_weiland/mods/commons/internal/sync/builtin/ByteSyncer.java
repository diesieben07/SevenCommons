package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class ByteSyncer extends SyncerForImmutable<Byte> {
    public ByteSyncer() {
        super(byte.class);
    }

    @Override
    public Byte writeAndUpdate(Byte value, Byte companion, MCDataOutput out) {
        out.writeByte(value);
        return value;
    }

    @Override
    public Byte read(Byte oldValue, Byte companion, MCDataInput in) {
        return in.readByte();
    }
}
