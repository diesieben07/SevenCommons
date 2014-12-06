package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
public final class GenericSyncer implements ValueSyncer<Object> {

    @Override
    public Object read(MCDataInputStream in, Object data) {
        return null;
    }

    @Override
    public boolean hasChanged(Object value, Object data) {
        return false;
    }

    @Override
    public Object writeAndUpdate(Object value, MCDataOutputStream out, Object data) {
        return null;
    }
}
