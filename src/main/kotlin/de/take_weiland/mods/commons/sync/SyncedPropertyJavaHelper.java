package de.take_weiland.mods.commons.sync;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import kotlin.reflect.KProperty1;
import kotlin.reflect.full.KClasses;
import kotlin.reflect.jvm.KCallablesJvm;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author diesieben07
 */
final class SyncedPropertyJavaHelper {

    @Nonnull
    static <T> List<KProperty1<? super T, ?>> getSyncedProperties(@Nonnull KClass<T> clazz, @Nonnull T obj) {
        List<KProperty1<? super T, ?>> result = new ArrayList<>();

        Class<? super T> javaClass = JvmClassMappingKt.getJavaClass(clazz);
        do {
            getSyncedProperties0(javaClass, obj, result);
        } while ((javaClass = javaClass.getSuperclass()) != null);

        return result;
    }

    private static <T> void getSyncedProperties0(@Nonnull Class<? super T> clazz, @Nonnull T obj, List<KProperty1<? super T, ?>> result) {
        Collection<? extends KProperty1<? super T, ?>> declaredMemberProperties = KClasses.getDeclaredMemberProperties(JvmClassMappingKt.getKotlinClass(clazz));
        for (KProperty1<? super T, ?> property : declaredMemberProperties) {
            KCallablesJvm.setAccessible(property, true);
            if (property.getDelegate(obj) instanceof BaseSyncedProperty) {
                result.add(property);
            }
        }
    }
}
