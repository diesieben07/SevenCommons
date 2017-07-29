package de.take_weiland.mods.commons.sync;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;

/**
 * @author Take Weiland
 */
final class PropertyAccessorHelpers {

    @Nullable
    static SyncedProperty<?> accessProperty(MethodHandle accessor, Object container, int id) throws Throwable {
        try {
            return (SyncedProperty<?>) accessor.invokeExact((Object) container, (int) id);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return null;
        }
    }

}
