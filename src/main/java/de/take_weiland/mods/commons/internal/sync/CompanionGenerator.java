package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.sync.SyncerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * @author diesieben07
 */
interface CompanionGenerator {

    public MethodHandle generateCompanionConstructor();

    final class SyncedMemberInfo {

        final Member member;
        final SyncerFactory.Handle handle;

        final MethodHandle getter;
        final MethodHandle setter;

        SyncedMemberInfo(Member member, SyncerFactory.Handle handle) {
            this.member = member;
            this.handle = handle;

            ((AccessibleObject) member).setAccessible(true);

            getter = getter();
            setter = setter();
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
