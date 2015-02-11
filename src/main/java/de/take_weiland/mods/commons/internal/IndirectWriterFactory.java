package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class IndirectWriterFactory implements NBTWriterFactory {

    private final MethodHandle provider;

    public IndirectWriterFactory(MethodHandle provider) {
        if (provider.type().parameterCount() == 0) {
            provider = MethodHandles.dropArguments(provider, 0, TypeSpecification.class);
        }
        this.provider = provider.asType(methodType(MethodHandle.class, TypeSpecification.class));
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        try {
            MethodHandle writer = (MethodHandle) provider.invokeExact((TypeSpecification<?>) typeSpec);
            return new DirectWriterFactory(writer).makeWriter(typeSpec, getter, setter);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
}
