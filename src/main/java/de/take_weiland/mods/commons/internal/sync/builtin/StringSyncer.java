package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class StringSyncer extends SyncerDefaultEquals<String> {
    public StringSyncer() {
        super(String.class);
    }

    @Override
    public String writeAndUpdate(String value, String companion, MCDataOutput out) {
        out.writeString(value);
        return value;
    }

    @Override
    public String read(String value, String companion, MCDataInput in) {
        return in.readString();
    }
}
