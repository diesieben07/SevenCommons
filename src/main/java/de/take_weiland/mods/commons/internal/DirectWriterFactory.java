package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class DirectWriterFactory implements NBTWriterFactory {

    private final MethodHandle writer;

    public DirectWriterFactory(MethodHandle writer) {
        this.writer = writer;
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        // C being the property holder class
        // T being the type of the property
        // S being the type that the writer handles (must be assignable from T)
        // NBT being the type of NBT that the writer produces
        //
        // writer has type (S) => NBT
        // getter has type (C) => T
        // we produce a method like this:
        // NBTBase write(C c) {
        //     T t = getter(c);
        //     if (t == null) {
        //         return serializedNull();
        //     } else {
        //         return writer(t);
        //     }

        Class<?> propertyType = getter.type().returnType();

        MethodHandle serNul = MethodHandles.dropArguments(SERIALIZED_NULL, 0, propertyType);
        MethodHandle adaptedWriter = writer.asType(methodType(NBTBase.class, propertyType));
        MethodHandle isNull = IS_NULL.asType(methodType(boolean.class, propertyType));

        MethodHandle nullSafeWriter = MethodHandles.guardWithTest(isNull, serNul, adaptedWriter);

        return MethodHandles.filterArguments(nullSafeWriter, 0, getter);
    }

    private static final MethodHandle SERIALIZED_NULL;
    private static final MethodHandle IS_NULL;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            SERIALIZED_NULL = lookup.findStatic(NBTData.class, "serializedNull", methodType(NBTBase.class));
            IS_NULL = lookup.findStatic(DirectWriterFactory.class, "isNull", methodType(boolean.class, Object.class));
        } catch(ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean isNull(@Nullable Object o) {
        return o == null;
    }
}
