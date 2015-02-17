package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.nbt.NBTBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
* @author diesieben07
*/
final class MethodHandleBasedFactory implements NBTSerializerFactory {

    private final MethodHandle filter;
    private final MethodHandle reader;
    private final MethodHandle writer;

    MethodHandleBasedFactory(MethodHandle filter, MethodHandle reader, MethodHandle writer) {
        this.filter = filter;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        if (valid(typeSpec)) {
            return bindReader(reader, getter, setter);
        } else {
            return null;
        }
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        return bindWriter(writer, getter, setter);
    }

    private boolean valid(TypeSpecification<?> spec) {
        try {
            return (boolean) filter.invokeExact(spec);
        } catch (Throwable t) {
            throw JavaUtils.throwUnchecked(t);
        }
    }

    static MethodHandle bindReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        NBTSerializers.validateGetterSetter(getter, setter);
        if (reader.type().returnType() == void.class) {
            return bindContentReader(reader, getter, setter);
        } else {
            return bindValueReader(reader, getter, setter);
        }
    }

    private static MethodHandle bindValueReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        // C being the property holder class
        // T being the type of the property
        // S being the type that the reader produces (must be assignable to T)
        // NBT being the NBT type that the reader handles
        //
        // reader has type (NBT) => S
        // setter has type (C, T) => void
        //
        // we produce a method like this:
        // void read(C c, NBTBase nbt) {
        //     if (nbt == <serialized null>) {
        //         setter(c, null);
        //     } else {
        //         if (nbt instanceof NBT) { // includes null check
        //             setter(c, reader((NBT) nbt));
        //         }
        //     }
        // }
        //
        // the conditions are optimized as possible

        Class<?> propertyType = setter.type().parameterType(1);
        if (propertyType.isPrimitive()) {
            return bindValueReaderPrim(reader, getter, setter);
        }

        Class<?> propertyHolder = setter.type().parameterType(0);
        Class<?> nbtClass = reader.type().parameterType(0);

        MethodHandle isSerNull = MethodHandles.dropArguments(NBTSerializerMethods.IS_SER_NULL, 0, propertyHolder);

        MethodHandle setNull = MethodHandles.insertArguments(setter, 1, (Object) null);
        setNull = MethodHandles.dropArguments(setNull, 1, NBTBase.class);

        MethodHandle isValid;

        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL, 0, propertyHolder);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_ID, 1, nbtID), 0, propertyHolder);
        }
        MethodHandle readSet = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(propertyType, NBTBase.class)));
        MethodHandle adaptedNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, propertyHolder, NBTBase.class);
        MethodHandle nonNullSet = MethodHandles.guardWithTest(isValid, readSet, adaptedNothing);

        return MethodHandles.guardWithTest(isSerNull, setNull, nonNullSet);
    }

    private static MethodHandle bindValueReaderPrim(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        Class<?> propertyHolder = setter.type().parameterType(0);
        Class<?> propertyType = setter.type().parameterType(1);
        Class<?> nbtClass = reader.type().parameterType(0);

        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL_AND_NOT_SERNULL, 0, propertyHolder);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_NONNULL_AND_ID, 1, nbtID), 0, propertyHolder);
        }

        MethodHandle readSet = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(propertyType, NBTBase.class)));
        MethodHandle doNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, propertyHolder, NBTBase.class);

        return MethodHandles.guardWithTest(isValid, readSet, doNothing);
    }

    private static MethodHandle bindContentReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        // C being the property holder class
        // T being the type of the property
        // S being the type that the reader handles (must be assignable from T)
        // NBT being the NBT type that the reader handles
        //
        // reader has type (NBT, S) => void
        // getter has type (C) => T
        //
        // we produce a method like this:
        // void read(C c, NBTBase nbt) {
        //     if (nbt != <serialized null> && nbt instanceof NBT) {
        //         reader(getter(c), (NBT) nbt);
        //     }
        // }
        //
        // the conditions are optimized as much as possible

        Class<?> propertyHolder = getter.type().parameterType(0);
        Class<?> propertyType = getter.type().returnType();
        Class<?> nbtClass = reader.type().parameterType(1);

        MethodHandle adaptedRead = reader.asType(methodType(void.class, propertyType, NBTBase.class));
        MethodHandle doRead = MethodHandles.filterArguments(adaptedRead, 0, getter);

        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL_AND_NOT_SERNULL, 0, propertyHolder);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_NONNULL_AND_ID, 1, nbtID), 0, propertyHolder);
        }

        MethodHandle adaptedDoNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, propertyHolder, NBTBase.class);
        return MethodHandles.guardWithTest(isValid, doRead, adaptedDoNothing);
    }

    static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
        NBTSerializers.validateGetterSetter(getter, setter);

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

        MethodHandle serNul = MethodHandles.dropArguments(NBTSerializerMethods.SERIALIZED_NULL, 0, propertyType);
        MethodHandle adaptedWriter = writer.asType(methodType(NBTBase.class, propertyType));
        MethodHandle isNull = NBTSerializerMethods.IS_NULL.asType(methodType(boolean.class, propertyType));

        MethodHandle nullSafeWriter = MethodHandles.guardWithTest(isNull, serNul, adaptedWriter);

        return MethodHandles.filterArguments(nullSafeWriter, 0, getter);
    }

}
