package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.internal.sync.SyncEvent;
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

    public static final int FIELD_ID_END = -1;
    public static final int FIRST_USABLE_ID = 0;

    public static final int SUPER_CALL = 0b01;
    @Deprecated
    public static final int FORCE_CHECK = 0b10;

    public static final String CHECK = "check",
            CHECK_IN_CONTAINER = "checkInContainer",
            APPLY_CHANGES = "applyChanges",
            READ = "read";

    /**
     * <p>Called to check for changes, does everything to make sure all connected clients are up to date.</p>
     *
     * @param instance the actual object
     * @param flags    flags, currently only SUPER_CALL
     * @param player   a player object for performing a full update, null means do partial update
     * @return the SyncEvent or null if nothing changed
     */
    // these methods are implemented here so we can just always call super
    public SyncEvent check(Object instance, int flags, EntityPlayerMP player) {
        return null;
    }

    public SyncEvent checkInContainer(Object instance, int flags, EntityPlayerMP player) {
        return null;
    }

    public int applyChanges(Object instance, ChangeIterator values) {
        return values.nextFieldId();
    }

    /**
     * <p>Used to iterate through the changes to be applied to a sync companion.</p>
     */
    public interface ChangeIterator {

        /**
         * <p>Get the next field ID. Returns -1 if no further data is present.</p>
         *
         * @return the next field ID
         */
        int nextFieldId();

        /**
         * <p>Apply the value for the previously obtained field ID to the given property using the syncer.</p>
         * <p>This will either call {@link TypeSyncer#apply(Object, Object, PropertyAccess, Object, PropertyAccess)} or
         * {@link TypeSyncer#apply(MCDataInput, Object, PropertyAccess, Object, PropertyAccess)}, depending of the type of
         * input.</p>
         *
         * @param syncer    the syncer
         * @param obj       the object containing the property
         * @param property  the property
         * @param cObj      the object containing the companion property
         * @param companion the companion property
         */
        <T_DATA, T_VAL, T_COM> void apply(TypeSyncer<T_VAL, T_COM, T_DATA> syncer, Object obj, PropertyAccess<T_VAL> property, Object cObj, PropertyAccess<T_COM> companion);


    }

}
