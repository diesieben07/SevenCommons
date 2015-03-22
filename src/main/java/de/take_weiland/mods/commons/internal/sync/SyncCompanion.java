package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * <p>Base class for @Sync companion</p>
 * @author diesieben07
 */
public abstract class SyncCompanion {

    /**
     * <p>Called to check for changes, does everything to make sure the client object is up to date.</p>
     * @param instance the actual object
     * @param isSuperCall if this is a super call from an extending companion (used to prevent sending the packet prematurely)
     * @return the OutputStream that is being written to, might be null
     */
    public abstract MCDataOutput check(Object instance, boolean isSuperCall);

    /**
     * <p>Called to read the data on the client.</p>
     * @param instance the actual object
     * @param in the InputStream
     * @return last read ID, 0 for end of stream
     */
    public abstract int read(Object instance, MCDataInput in);

}
