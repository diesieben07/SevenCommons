package de.take_weiland.mods.commons.reflect;

import com.google.common.base.Throwables;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class ProxyInterfaceImplementer implements InterfaceImplementer {

    @Override
    public <T> Supplier<T> implement(ClassLoader loader, Collection<TypeToken<?>> interfaces, Function<Method, MethodHandle> implementationProvider) {
        Map<Method, MethodHandle> implementations = interfaces.stream()
                .flatMap(i -> Stream.of(i.getRawType().getMethods()))
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .collect(Collectors.toMap(Function.identity(), implementationProvider));

        InvocationHandler handler = new AbstractInvocationHandler() {

            @Override
            protected Object handleInvocation(@Nonnull Object proxy, @Nonnull Method method, @Nonnull Object[] args) throws Throwable {
                return implementations.get(method).invoke(args);
            }

            @Override
            public String toString() {
                return "ProxyInterfaceImplementor for " + interfaces;
            }
        };

        Class<?>[] toImplement = interfaces.stream()
                .map(TypeToken::getRawType)
                .toArray(Class[]::new);

        Class<?> clazz = Proxy.getProxyClass(loader, toImplement);
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(InvocationHandler.class);
            constructor.setAccessible(true);
            MethodHandle mh = MethodHandles.publicLookup().unreflectConstructor(constructor)
                    .bindTo(handler)
                    .asType(methodType(Object.class));
            return () -> {
                try {
                    //noinspection unchecked
                    return (T) (Object) mh.invokeExact();
                } catch (Throwable x) {
                    throw Throwables.propagate(x);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
