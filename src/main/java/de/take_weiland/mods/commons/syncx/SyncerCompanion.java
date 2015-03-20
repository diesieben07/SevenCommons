package de.take_weiland.mods.commons.syncx;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * @author diesieben07
 */
public abstract class SyncerCompanion {

    public abstract MCDataOutput check(Object instance, boolean isSuperCall);
    public abstract void read(Object instance, MCDataInput in);

}
