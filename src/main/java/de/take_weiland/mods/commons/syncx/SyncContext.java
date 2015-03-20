package de.take_weiland.mods.commons.syncx;

import java.lang.invoke.MethodHandle;

/**
 * @author diesieben07
 */
public interface SyncContext {

    Class<?> companionHolderType();

    CompanionData newCompanion(Class<?> type);

    public interface CompanionData {

        Class<?> type();

        MethodHandle get();

        MethodHandle set();

    }

}
