package de.take_weiland.mods.commons.internal.prop;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.Property;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author diesieben07
 */
public abstract class AbstractProperty<T, MEM extends AccessibleObject & Member & AnnotatedElement> implements Property<T, MEM> {

    public static <T> Property<T, ?> newProperty(Member member) {
        if (member instanceof Field) {
            return new FieldProperty<>((Field) member);
        } else if (member instanceof Method) {
            return new MethodProperty<>((Method) member);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static Function<Member, Property<?, ?>> getPropertyFunction() {
        return new Function<Member, Property<?, ?>>() {
            @Override
            public Property<?, ?> apply(Member input) {
                return newProperty(input);
            }
        };
    }

    public static List<Property<?, ?>> allProperties(Class<?> clazz, Class<? extends Annotation> annotation) {
        return allPropertiesLazy(clazz, annotation).toList();
    }

    public static FluentIterable<Property<?, ?>> allPropertiesLazy(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getPropertySourceClasses(clazz)
                .transformAndConcat(getDirectProperties(isAnnotatedWith(annotation)));
    }

    private static Function<Class<?>, Iterable<Property<?, ?>>> getDirectProperties(final Predicate<Member> filter) {
        return new Function<Class<?>, Iterable<Property<?, ?>>>() {
            @Nullable
            @Override
            public Iterable<Property<?, ?>> apply(Class<?> input) {
                return getDirectProperties(input, filter);
            }
        };
    }

    private static Iterable<Property<?, ?>> getDirectProperties(Class<?> clazz, Predicate<Member> filter) {
        Iterable<Member> methods = Arrays.<Member>asList(clazz.getDeclaredMethods());
        Iterable<Member> fields = Arrays.<Member>asList(clazz.getDeclaredFields());

        return FluentIterable.from(Iterables.concat(methods, fields))
                .filter(filter)
                .transform(getPropertyFunction());
    }

    private static Predicate<Member> isAnnotatedWith(final Class<? extends Annotation> annotation) {
        return new Predicate<Member>() {
            @Override
            public boolean apply(Member input) {
                return ((AnnotatedElement) input).isAnnotationPresent(annotation);
            }
        };
    }

    private static FluentIterable<Class<?>> getPropertySourceClasses(Class<?> clazz) {
        FluentIterable<Class<?>> newIfaces = FluentIterable.from(Arrays.asList(clazz.getInterfaces()))
                .filter(isNewInterface(clazz));

        return FluentIterable.from(
                Iterables.concat(Collections.singleton(clazz), newIfaces)
        );
    }

    private static Predicate<Class<?>> isNewInterface(final Class<?> currentClass) {
        return new Predicate<Class<?>>() {
            @Override
            public boolean apply(Class<?> input) {
                return !input.isAssignableFrom(currentClass.getSuperclass());
            }
        };
    }

    final MEM member;
    private final SerializationMethod.Method desiredMethod;
    private TypeToken<T> genericType;
    private MethodHandle getter;
    private MethodHandle setter;

    AbstractProperty(MEM member) {
        this.member = member;
        SerializationMethod annotation = member.getAnnotation(SerializationMethod.class);
        desiredMethod = annotation == null ? SerializationMethod.Method.DEFAULT : annotation.value();
    }

    @Override
    public final MEM getMember() {
        return member;
    }

    @Override
    public final SerializationMethod.Method getDesiredMethod() {
        return desiredMethod;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final TypeToken<T> getType() {
        return genericType == null ? (genericType = (TypeToken<T>) resolveType()) : genericType;
    }

    @Override
    public final MethodHandle getGetter() {
        if (getter == null) {
            member.setAccessible(true);
            try {
                getter = resolveGetter();
            } catch (IllegalAccessException e) {
                throw new AssertionError(e); // impossible
            }
        }
        return getter;
    }

    @Override
    public MethodHandle getSetter() {
        if (setter == null) {
            member.setAccessible(true);
            try {
                setter = resolveSetter();
            } catch (IllegalAccessException e) {
                throw new AssertionError(e); // impossible
            }
        }
        return setter;
    }

    abstract TypeToken<?> resolveType();

    abstract MethodHandle resolveGetter() throws IllegalAccessException;
    abstract MethodHandle resolveSetter() throws IllegalAccessException;

    @Override
    public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return member.getAnnotation(annotationClass);
    }

    @Override
    public final boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return member.isAnnotationPresent(annotation);
    }

    @Override
    public final String toString() {
        return "TypeSpec of type " + getType();
    }

}
