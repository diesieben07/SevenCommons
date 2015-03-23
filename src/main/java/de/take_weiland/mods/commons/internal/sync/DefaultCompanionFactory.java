package de.take_weiland.mods.commons.internal.sync;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class DefaultCompanionFactory implements CompanionFactory {

    private static final MethodHandle NULL_COMPANION = MethodHandles.constant(SyncCompanion.class, null);
    private final Map<Class<?>, Class<?>> companionClasses = new HashMap<>();

    @Override
    public MethodHandle getCompanionConstructor(Class<?> clazz) {
        Class<?> companionClass = getCompanionClass(clazz);
        if (companionClass == null) {
            return NULL_COMPANION;
        } else {
            try {
                return lookup().findConstructor(companionClass, methodType(void.class))
                        .asType(methodType(SyncCompanion.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e); // impossible
            }
        }
    }

    Class<?> getCompanionClass(Class<?> clazz) {
        if (clazz == Object.class) {
            return null;
        }
        Class<?> companionClass = companionClasses.get(clazz);
        if (companionClass == null) {
            companionClass = new BytecodeEmittingCompanionGenerator(this, clazz).generateCompanion();
            if (companionClass == null) {
                companionClass = getCompanionClass(clazz.getSuperclass());
            }
            companionClasses.put(clazz, companionClass);
        }
        return companionClass;
    }

    List<SyncedMemberInfo> getSyncedMemberInfo(Class<?> clazz) {
        return CompanionGenerators.getSyncedMemberInfo(clazz);
    }

    int getNextFreeIDFor(Class<?> clazz) {
        if (clazz == Object.class) {
            return 1; // ID 0 is taken for end of stream
        }
        Class<?> superclass = clazz.getSuperclass();
        return getNextFreeIDFor(superclass) + CompanionGenerators.getSyncedMembers(superclass).size();
    }

}
