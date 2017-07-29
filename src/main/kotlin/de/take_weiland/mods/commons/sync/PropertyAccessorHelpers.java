package de.take_weiland.mods.commons.sync;

import java.lang.invoke.MethodHandle;

/**
 * @author Take Weiland
 */
final class PropertyAccessorHelpers {

    static SyncedProperty<?> accessProperty(MethodHandle accessor, Object container, int id) throws Throwable {
        return (SyncedProperty<?>) accessor.invokeExact((Object) container, (int) id);
    }

}
