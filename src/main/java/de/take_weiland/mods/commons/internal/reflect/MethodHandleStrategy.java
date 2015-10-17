package de.take_weiland.mods.commons.internal.reflect;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class MethodHandleStrategy extends ReflectionStrategy {

    @Override
    public <T> T createAccessor(Class<T> iface) {
        validateInterface(iface);

        ImmutableMap<Method, MethodHandle> handles = FluentIterable.from(Arrays.asList(iface.getDeclaredMethods()))
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .toMap(AccessorMemberParser::getTarget);

        CompileContextImpl context = AccessorCompiler.emitStart(Type.getInternalName(iface));
        for (Map.Entry<Method, MethodHandle> entry : handles.entrySet()) {
            Method method = entry.getKey();
            MethodHandle targetHandle = entry.getValue();
            AccessorCompiler.emitDelegateMethod(context, method.getName(), methodType(method.getReturnType(), method.getParameterTypes()), targetHandle);
        }

        //noinspection unchecked
        return (T) context.linkInstantiate();
    }

    public static Iterator<?> staticData;

}
