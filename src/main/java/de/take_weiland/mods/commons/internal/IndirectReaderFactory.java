package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class IndirectReaderFactory implements NBTReaderFactory {

    private final MethodHandle provider;

    public IndirectReaderFactory(MethodHandle provider) {
        if (provider.type().parameterCount() == 0) {
            provider = MethodHandles.dropArguments(provider, 0, TypeSpecification.class);
        }
        this.provider = provider.asType(methodType(MethodHandle.class, TypeSpecification.class));
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        try {
            MethodHandle reader = (MethodHandle) provider.invokeExact(typeSpec);
            return new DirectReaderFactory(reader).makeReader(typeSpec, getter, setter);
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }
}
