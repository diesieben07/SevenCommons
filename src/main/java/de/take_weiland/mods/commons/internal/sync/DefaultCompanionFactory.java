package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.sync.Syncer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class DefaultCompanionFactory implements CompanionFactory {

    private static final MethodHandle NULL_COMPANION = MethodHandles.constant(SyncCompanion.class, null);
    private final Map<Class<?>, CompanionClassInfo> companionClasses = new HashMap<>();

    @Override
    public MethodHandle getCompanionConstructor(Class<?> clazz) {
        Class<?> companionClass = getCompanionClass(clazz);
        if (companionClass == null) {
            return NULL_COMPANION;
        } else {
            try {
                return publicLookup().findConstructor(companionClass, methodType(void.class))
                        .asType(methodType(SyncCompanion.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e); // impossible
            }
        }
    }

    Class<?> getCompanionClass(Class<?> clazz) {
        CompanionClassInfo info = getInfo(clazz);
        return info == null ? null : info.clazz;
    }

    private CompanionClassInfo getInfo(Class<?> clazz) {
        if (clazz == Object.class) {
            return null;
        }
        CompanionClassInfo info;
        if (!companionClasses.containsKey(clazz)) {
            Map<Property<?, ?>, Syncer<?, ?, ?>> members = SyncCompanions.getSyncedMemberInfo(clazz);
            if (members.isEmpty()) {
                info = null;
            } else {
                Class<?> companionClass = new BytecodeEmittingCompanionGenerator(this, clazz, members).generateCompanion();
                info = new CompanionClassInfo(companionClass, members.size());
            }
            companionClasses.put(clazz, info);
        } else {
            info = companionClasses.get(clazz);
        }
        return info;
    }

    int getNextFreeIDFor(Class<?> clazz) {
        if (clazz == Object.class) {
            return SyncCompanion.FIRST_USEABLE_ID; // ID 0 is taken for end of stream
        }

        Class<?> superclass = clazz.getSuperclass();
        CompanionClassInfo info = getInfo(superclass);

        return getNextFreeIDFor(superclass) + (info == null ? 0 : info.numSyncedMembers);
    }

    private static final class CompanionClassInfo {

        final Class<?> clazz;
        final int numSyncedMembers;

        CompanionClassInfo(Class<?> clazz, int numSyncedMembers) {
            this.clazz = clazz;
            this.numSyncedMembers = numSyncedMembers;
        }
    }

}
