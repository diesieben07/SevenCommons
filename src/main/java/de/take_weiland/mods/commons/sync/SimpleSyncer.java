package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
public interface SimpleSyncer<V, C> {

    Class<V> getValueType();

    Class<C> getCompanionType();

    boolean equal(V value, C companion);

    C writeAndUpdate(V value, C companion, MCDataOutput out);

    V read(V oldValue, C companion, MCDataInput in);

}
