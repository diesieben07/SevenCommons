package de.take_weiland.mods.commons.sync;

import com.google.common.collect.ImmutableList;
import kotlin.Pair;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KProperty1;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * @author diesieben07
 */
final class SyncedPropertyJavaHelper {

    static <T> Pair<KClass<? super T>, KProperty1<? super T, ?>> getPropertyInfo(BaseSyncedProperty property, Class<? super T> baseClass, T obj) {
        Class<? super T> currentClass = baseClass;
        do {
            KClass<? super T> kotlinClass = JvmClassMappingKt.getKotlinClass(currentClass);
            Collection<? extends KProperty1<? super T, ?>> declaredMemberProperties = KClasses.getDeclaredMemberProperties(kotlinClass);
            for (KProperty1<? super T, ?> declaredMemberProperty : declaredMemberProperties) {
                KCallablesJvm.setAccessible(declaredMemberProperty, true);
                if (declaredMemberProperty.getDelegate(obj) == property) {
                    return new Pair<>(kotlinClass, declaredMemberProperty);
                }
            }
        } while ((currentClass = currentClass.getSuperclass()) != null);
        throw new IllegalArgumentException("Property not in class hierarchy");
    }

    static <T> int getPropertyId(@Nonnull KProperty1<? super T, ?> property, KClass<? super T> clazz, T obj) {
        List<? extends KProperty1<? super T, ?>> declaredMemberProperties = ImmutableList.copyOf(KClasses.getDeclaredMemberProperties(clazz));
        int id = 0;
        for (KProperty1<? super T, ?> memberProperty : declaredMemberProperties) {
            if (memberProperty.equals(property)) {
                return id;
            } else {
                KCallablesJvm.setAccessible(memberProperty, true);
                if (memberProperty.getDelegate(obj) instanceof BaseSyncedProperty) {
                    id++;
                }
            }
        }
        throw new IllegalArgumentException("Property not part of class.");
    }

}
