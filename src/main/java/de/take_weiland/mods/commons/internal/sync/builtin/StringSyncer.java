package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

/**
 * @author diesieben07
 */
enum StringSyncer implements Syncer.ForImmutable<String> {

    INSTANCE;

    @Override
    public Class<String> getCompanionType() {
        return String.class;
    }

    @Override
    public void write(String value, MCDataOutput out) {
        out.writeString(value);
    }

    @Override
    public String read(MCDataInput in) {
        return in.readString();
    }
}
