package de.take_weiland.mods.commons.internal.sync;

import java.lang.invoke.MethodHandle;

/**
 * <p>Factory for creating Companion objects.</p>
 * @author diesieben07
 */
interface CompanionFactory {

    /**
     * <p>Create a MethodHandle for creating a new companion object for the given class.</p>
     * <p>This MethodHandle must have the exact type ()=>SyncCompanion.</p>
     * <p>If the class implements IExtendedEntityProperties the resulting type must also implement IEEPSyncCompanion.</p>
     * @param clazz the class to generate a companion for
     * @return a MethodHandle
     */
    MethodHandle getCompanionConstructor(Class<?> clazz);

}
