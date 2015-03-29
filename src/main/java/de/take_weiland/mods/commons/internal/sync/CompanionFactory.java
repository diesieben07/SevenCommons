package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.sync.Syncer;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.publicLookup;

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

    /**
     * <p>Info about a Member marked with @Sync.</p>
     */
    final class SyncedMemberInfo {

        final Member member;
        final Syncer<?, ?> syncer;
        final Method setterMethod;

        final MethodHandle getter;
        final MethodHandle setter;

        SyncedMemberInfo(Class<?> clazz, Member member, Syncer<?, ?> syncer) {
            this.member = member;
            this.syncer = syncer;

            ((AccessibleObject) member).setAccessible(true);
            if (member instanceof Field) {
                setterMethod = null;
            } else {
                setterMethod = SCReflection.findSetter((Method) member);
            }

            // need to potentially adjust the types in case the
            // member is in an interface. in any case this is a null-conversion and does not occur any overhead
            // except (potentially) changing the exact type of the MethodHandle
            MethodHandle getter = getter();
            this.getter = getter.asType(getter.type().changeParameterType(0, clazz));

            MethodHandle setter = setter();
            this.setter = setter.asType(setter.type().changeParameterType(0, clazz));
        }

        Class<?> type() {
            return member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
        }

        private MethodHandle getter() {
            try {
                if (member instanceof Method) {
                    return publicLookup().unreflect((Method) member);
                } else {
                    return publicLookup().unreflectGetter((Field) member);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // impossible
            }
        }

        private MethodHandle setter() {
            try {
                if (member  instanceof Method) {
                    Method setter = SCReflection.findSetter((Method) member);
                    setter.setAccessible(true);
                    return publicLookup().unreflect(setter);
                } else {
                    return publicLookup().unreflectSetter((Field) member);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // impossible
            }
        }


    }
}
