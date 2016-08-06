package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * <p>Base class for @Sync companion</p>
 *
 * @author diesieben07
 */
public abstract class SyncCompanion {

    public static final int FIELD_ID_END = 0;
    public static final int FIRST_USEABLE_ID = 1;

    public static final int SUPER_CALL = 0b01;
    @Deprecated public static final int FORCE_CHECK = 0b10;

    public static final String CHECK = "check",
            CHECK_IN_CONTAINER = "checkInContainer",
            APPLY_CHANGES = "applyChanges",
            READ = "read";

    /**
     * <p>Called to check for changes, does everything to make sure the client object is up to date.</p>
     *
     * @param instance the actual object
     * @param flags    flags, currently only SUPER_CALL
     * @param player a player object for performing a full update, null means do partial update
     * @return the SyncEvent or null if nothing changed
     */
    public SyncEvent check(Object instance, int flags, EntityPlayerMP player) {
        // implemented here so we can just always call super
        return null;
    }

    public abstract SyncEvent checkInContainer(Object instance, int flags, EntityPlayerMP player);

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

        <T_DATA, T_VAL, T_COM> void apply(TypeSyncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion);


    }

}
