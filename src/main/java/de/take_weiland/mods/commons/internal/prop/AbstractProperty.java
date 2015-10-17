package de.take_weiland.mods.commons.internal.prop;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.reflect.AccessorCompiler;
import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author diesieben07
 */
public abstract class AbstractProperty<T, MEM extends AccessibleObject & Member & AnnotatedElement> implements Property<T> {

    public static Property<?> newProperty(AnnotatedElement member) {
        if (member instanceof Field) {
            return new FieldProperty<>((Field) member);
        } else if (member instanceof Method) {
            return new MethodProperty<>((Method) member);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static List<Property<?>> getProperties(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<AnnotatedElement> members = new ArrayList<>();
        Iterables.addAll(members, getDirectMembers(clazz));

        for (Class<?> iface : getNotInhertiedInterfaces(clazz)) {
            Iterables.addAll(members, getDirectMembers(iface));
        }

        ImmutableList.Builder<Property<?>> b = ImmutableList.builder();
        for (AnnotatedElement member : members) {
            if (annotation == null || member.isAnnotationPresent(annotation)) {
                b.add(newProperty(member));
            }
        }
        return b.build();
    }

    private static Iterable<AnnotatedElement> getDirectMembers(Class<?> clazz) {
        return Iterables.concat(Arrays.<AnnotatedElement>asList(clazz.getDeclaredMethods()), Arrays.<AnnotatedElement>asList(clazz.getDeclaredFields()));
    }

    private static Iterable<Class<?>> getNotInhertiedInterfaces(Class<?> clazz) {
        return asFluent(clazz.getInterfaces())
                .filter(it -> !ArrayUtils.contains(clazz.getSuperclass().getInterfaces(), it))
                .toList();
    }

    public static List<Property<?>> allProperties(Class<?> clazz, Class<? extends Annotation> annotation) {
        return allPropertiesLazy(clazz, annotation).toList();
    }

    public static FluentIterable<Property<?>> allPropertiesLazy(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getPropertySourceClasses(clazz)
                .transformAndConcat(sourceClass -> getDirectProperties(sourceClass, element -> element.isAnnotationPresent(annotation)));
    }

    private static Iterable<Property<?>> getDirectProperties(Class<?> clazz, Predicate<AnnotatedElement> filter) {
        Iterable<AnnotatedElement> methods = asFluent(clazz.getDeclaredMethods());
        Iterable<AnnotatedElement> fields = asFluent(clazz.getDeclaredFields());
        return concat(methods, fields)
                .filter(filter)
                .transform(AbstractProperty::newProperty);
    }

    private static FluentIterable<Class<?>> getPropertySourceClasses(Class<?> clazz) {
        FluentIterable<Class<?>> newIfaces = asFluent(clazz.getInterfaces())
                .filter(input -> !input.isAssignableFrom(clazz.getSuperclass()));

        return concat(Collections.singleton(clazz), newIfaces);
    }

    @SafeVarargs
    private static <T> FluentIterable<T> asFluent(T... arr) {
        return FluentIterable.from(Arrays.asList(arr));
    }

    private static <T> FluentIterable<T> asFluent(Iterable<? extends T> it) {
        //noinspection unchecked
        return FluentIterable.from((Iterable<T>) it);
    }

    private static <T> FluentIterable<T> concat(Iterable<T> a, Iterable<T> b) {
        return asFluent(Iterables.concat(a, b));
    }

    final MEM member;
    private TypeToken<T> genericType;
    private PropertyAccess<T> optimized;

    AbstractProperty(MEM member) {
        this.member = member;
        member.setAccessible(true);
    }

    @Override
    public final Member getMember() {
        return member;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final TypeToken<T> getType() {
        return genericType == null ? (genericType = (TypeToken<T>) resolveType()) : genericType;
    }

    @Override
    public synchronized final PropertyAccess<T> optimize() {
        if (optimized == null) {
            optimized = AccessorCompiler.makeOptimizedProperty(member, this);
        }
        return optimized;
    }

//    abstract PropertyAccess<T> doOptimize();

    abstract TypeToken<?> resolveType();

    @Override
    public final String toString() {
        return "TypeSpec of type " + getType();
    }

    static RuntimeException immutableEx() {
        return new UnsupportedOperationException("Immutable Property");
    }

}
