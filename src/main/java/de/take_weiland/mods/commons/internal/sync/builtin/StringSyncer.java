package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;

/**
 * @author diesieben07
 */
final class StringSyncer extends AbstractSyncer.ForImmutable<String> {

    protected <OBJ> StringSyncer(OBJ obj, PropertyAccess<OBJ, String> property) {
        super(obj, property);
    }

    @Override
    protected String decode(MCDataInput in) {
        return in.readString();
    }

    @Override
    public void encode(String value, MCDataOutput out) {
        out.writeString(value);
    }
}
