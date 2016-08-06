package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.TypeSyncer;

/**
 * @author diesieben07
 */
enum StringSyncer implements TypeSyncer.ForImmutable<String> {

    INSTANCE;

    @Override
    public String decode(MCDataInput in) {
        return in.readString();
    }

    @Override
    public void encode(String s, MCDataOutput out) {
        out.writeString(s);
    }

    @Override
    public Class<String> companionType() {
        return String.class;
    }
}
