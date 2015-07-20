package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * <p>Base class for @Sync companion</p>
 *
 * @author diesieben07
 */
public abstract class SyncCompanion {

    public static final int FIELD_ID_END = 0;
    public static final int FIRST_USEABLE_ID = 1;

    /**
     * <p>Called to check for changes, does everything to make sure the client object is up to date.</p>
     *
     * @param instance    the actual object
     * @param isSuperCall if this is a super call from an extending companion (used to prevent sending the packet prematurely)
     * @return the SyncEvent or null if nothing changed
     */
    public abstract SyncEvent check(Object instance, boolean isSuperCall);

    public abstract SyncEvent forceUpdate(Object instance, boolean isSuperCall, EntityPlayerMP player);

    public abstract int applyChanges(Object instance, ChangeIterator values);

    /**
     * <p>Called to read the data on the client.</p>
     *
     * @param instance the actual object
     * @param in       the InputStream
     * @return last read ID, 0 for end of stream
     */
    public abstract int read(Object instance, MCDataInput in);

    public interface ChangeIterator {

        int fieldId();

        <T_DATA, T_VAL, T_COM> void apply(Syncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion);


    }

}
