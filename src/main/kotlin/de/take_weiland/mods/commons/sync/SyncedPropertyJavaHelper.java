package de.take_weiland.mods.commons.sync;

import com.google.common.collect.ImmutableList;
import kotlin.Pair;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KProperty;
import kotlin.reflect.KProperty1;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;
import kotlin.reflect.jvm.ReflectJvmMapping;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

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

    static int getPropertyId(KProperty1<?, ?> property, Object obj) {
        Class<?> declaringClass = checkNotNull(ReflectJvmMapping.getJavaGetter(property)).getDeclaringClass();
        KClass<?> kotlinClass = JvmClassMappingKt.getKotlinClass(declaringClass);

        List<? extends KProperty1<?, ?>> declaredMemberProperties = ImmutableList.copyOf(KClasses.getDeclaredMemberProperties(kotlinClass));
        int id = 0;
        for (KProperty1<?, ?> memberProperty : declaredMemberProperties) {
            if (memberProperty.equals(property)) {
                return (id << SyncCapabilityKt.CLASS_ID_BITS) | SyncCapabilityKt.getId(kotlinClass);
            } else {
                KCallablesJvm.setAccessible(memberProperty, true);
                if (((KProperty1) memberProperty).getDelegate(obj) instanceof BaseSyncedProperty) {
                    id++;
                }
            }
        }
        throw new IllegalArgumentException("Property not part of class.");
    }

}
