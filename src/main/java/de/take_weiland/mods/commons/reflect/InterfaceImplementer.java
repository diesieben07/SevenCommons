package de.take_weiland.mods.commons.reflect;

import com.google.common.reflect.TypeToken;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author diesieben07
 */
public interface InterfaceImplementer {

    InterfaceImplementer DEFAULT = new ProxyInterfaceImplementer();

    <T> Supplier<T> implement(ClassLoader loader, Collection<TypeToken<?>> interfaces, Function<Method, MethodHandle> implementationProvider);

}
