package de.take_weiland.mods.commons.internal.prop;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.reflect.AccessorCompiler;
import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.reflect.PropertyAccess;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public static Stream<Property<?>> allProperties(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getPropertySourceClasses(clazz)
                .flatMap(c -> getDirectProperties(c, element -> element.isAnnotationPresent(annotation)));
    }

    private static Stream<Property<?>> getDirectProperties(Class<?> clazz, Predicate<AnnotatedElement> filter) {
        Stream<AnnotatedElement> methods = Arrays.stream(clazz.getDeclaredMethods());
        Stream<AnnotatedElement> fields = Arrays.stream(clazz.getDeclaredFields());
        return Stream.concat(methods, fields)
                .filter(filter)
                .map(AbstractProperty::newProperty);
    }

    private static Stream<Class<?>> getPropertySourceClasses(Class<?> clazz) {
        Stream<? extends Class<?>> stream = Arrays.stream(clazz.getInterfaces())
                .filter(input -> !input.isAssignableFrom(clazz.getSuperclass()));

        return Stream.concat(Stream.of(clazz), stream);
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
