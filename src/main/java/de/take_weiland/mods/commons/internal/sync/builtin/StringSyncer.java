package de.take_weiland.mods.commons.internal.sync.builtin;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SimpleSyncer;

/**
 * @author diesieben07
 */
public enum StringSyncer implements SimpleSyncer<String, String> {
    INSTANCE;

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public Class<String> getCompanionType() {
        return String.class;
    }

    @Override
    public boolean equal(String value, String companion) {
        return Objects.equal(value, companion);
    }

    @Override
    public String writeAndUpdate(String value, String companion, MCDataOutput out) {
        out.writeString(value);
        return value;
    }

    @Override
    public String read(String oldValue, String companion, MCDataInput in) {
        return in.readString();
    }
}
