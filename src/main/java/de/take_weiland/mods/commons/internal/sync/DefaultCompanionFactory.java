package de.take_weiland.mods.commons.internal.sync;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class DefaultCompanionFactory implements CompanionFactory {

    private final Map<Class<?>, Class<?>> companionClasses = new HashMap<>();
    private final Map<Class<?>, MethodHandle> companionConstructors = new HashMap<>();
    private final Map<Class<?>, List<SyncedMemberInfo>> syncedMemberData = new HashMap<>();
    private final Map<Class<?>, Integer> nextFreeID = new HashMap<>();

    @Override
    public MethodHandle getCompanionConstructor(Class<?> clazz) {
        MethodHandle cstr = companionConstructors.get(clazz);
        if (cstr == null) {
            Class<?> companionClass = getCompanionClass(clazz);
            if (companionClass == null) {
                throw new IllegalArgumentException("Cannot make constructor for class without synced members");
            }
            try {
                // constructors are package private so we can access them
                cstr = lookup().findConstructor(companionClass, methodType(void.class)).asType(methodType(SyncCompanion.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Internal Error during companion generation", e);
            }
            companionConstructors.put(clazz, cstr);
        }
        return cstr;
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
        List<SyncedMemberInfo> list = syncedMemberData.get(clazz);
        if (list == null) {
            list = CompanionGenerators.getSyncedMemberInfo(clazz);
            syncedMemberData.put(clazz, list);
        }
        return list;
    }

    int getNextFreeIDFor(Class<?> clazz) {
        if (clazz == Object.class) {
            return 1; // ID 0 is taken for end of stream
        }
        Integer id = nextFreeID.get(clazz);
        if (id == null) {
            id = getNextFreeIDFor(clazz.getSuperclass()) + getSyncedMemberInfo(clazz.getSuperclass()).size();
            nextFreeID.put(clazz, id);
        }
        return id;
    }

}
