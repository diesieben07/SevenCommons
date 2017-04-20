package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.internal.prop.AbstractProperty;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;

/**
 * @author diesieben07
 */
final class PropertySupport {

    public static void main(String[] args) {
        Stream<String> s = getAll(Map.Entry.class, m -> true, f -> true).map(Object::toString);
        System.out.println(s.collect(Collectors.joining("\n")));
    }

    static Stream<Property<?>> getAll(Class<?> clazz, Predicate<? super Method> methodFilter, Predicate<? super Field> fieldFilter) {
        Stream<Field> fields = JavaUtils.stream(ClassUtils.hierarchy(clazz))
                .flatMap(c -> Arrays.stream(c.getDeclaredFields()));

        Stream<Method> methods = getUniqueMethods(clazz);

        return Stream.concat(fields, methods)
                .filter(e -> !Modifier.isStatic(e.getModifiers()))
                .filter(e -> e instanceof Method ? methodFilter.test((Method) e) : fieldFilter.test((Field) e))
                .map(AbstractProperty::newProperty);
    }

    /**
     * Returns a stream of all methods, leaving out overridden ones
     * @param clazz the class
     * @return the stream of methods
     */
    private static Stream<Method> getUniqueMethods(Class<?> clazz) {
        return stream(ClassUtils.hierarchy(clazz, ClassUtils.Interfaces.INCLUDE))
                .map(Class::getDeclaredMethods)
                .flatMap(Stream::of)
                .filter(isUsefulMethod())
                .collect(groupingBy(groupOverrideChain(clazz)))
                .values().stream()
                .flatMap(l -> l.stream().limit(1));
    }

    private static Predicate<Method> isUsefulMethod() {
        return m -> !Modifier.isStatic(m.getModifiers()) && !m.isBridge() && !m.isSynthetic() && m.getReturnType() != void.class && m.getParameterCount() == 0;
    }

    private static <T> Stream<T> stream(Iterable<T> it) {
        return it instanceof Collection ? ((Collection<T>) it).stream() : StreamSupport.stream(it.spliterator(), false);
    }

    private static Function<Method, Object> groupOverrideChain(Class<?> base) {
        return m -> Pair.of(m.getName(), getResolvedParameterTypes(base, m));
    }

    private static List<Type> getResolvedParameterTypes(Class<?> base, Method method) {
        TypeToken<?> bt = TypeToken.of(base);
        return Stream.of(method.getGenericParameterTypes())
                .map(bt::resolveType)
                .map(TypeToken::getType)
                .collect(Collectors.toList());
    }

}
