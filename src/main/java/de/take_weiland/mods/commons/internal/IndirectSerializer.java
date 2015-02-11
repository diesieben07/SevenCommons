package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.serialize.DirectNBTSerializer;
import de.take_weiland.mods.commons.serialize.TypeSpecification;

import java.lang.invoke.MethodHandle;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class IndirectSerializer extends NBTSerializerWrapper {

    private final MethodHandle readerProvider;
    private final MethodHandle writerProvider;

    IndirectSerializer(MethodHandle readerProvider, MethodHandle writerProvider) {
        this.readerProvider = checkProvider(readerProvider);
        this.writerProvider = checkProvider(writerProvider);
    }

    private static MethodHandle checkProvider(MethodHandle provider) {
        checkArgument(provider.type().equals(methodType(MethodHandle.class, TypeSpecification.class)));
        return provider;
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        try {
            MethodHandle reader = (MethodHandle) readerProvider.invokeExact((TypeSpecification<?>) typeSpec);
            return DirectNBTSerializer.makeReader0(reader, getter, setter);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        try {
            MethodHandle writer = (MethodHandle) writerProvider.invokeExact((TypeSpecification<?>) typeSpec);
            return DirectNBTSerializer.makeWriter0(writer, getter);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

}
