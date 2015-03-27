package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
class CharSyncer extends SyncerForImmutable<Character> {

    public CharSyncer() {
        super(char.class);
    }

    @Override
    public Character writeAndUpdate(Character value, Character companion, MCDataOutput out) {
        out.writeChar(value);
        return value;
    }

    @Override
    public Character read(Character oldValue, Character companion, MCDataInput in) {
        return in.readChar();
    }
}
