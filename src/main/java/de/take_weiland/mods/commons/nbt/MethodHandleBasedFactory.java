package de.take_weiland.mods.commons.nbt;

import com.google.common.collect.Lists;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.nbt.NBTBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

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
        // T being the type of the property
        // X0-Xn being the data types needed to access the property via getter and setter
        //       (in case of a simple "field in a class" this is just a reference of that class)
        //       these might be no types at all
        //
        // S being the type that the reader produces (must be assignable to T)
        // NBT being the NBT type that the reader handles
        //
        // reader has type (NBT) => S
        // setter has type (X0, ..., XN, T) => void
        // getter has type (X0, ..., XN) => T
        //
        // we produce a method like this:
        // void read(X0 x0, ...,XN xn, NBTBase nbt) {
        //     if (nbt == <serialized null>) {
        //         setter(x0, ..., xn, null);
        //     } else {
        //         if (nbt instanceof NBT) { // includes null check
        //             setter(x0, ..., xn, reader((NBT) nbt));
        //         }
        //     }
        // }
        //
        // the conditions are optimized as possible

        // X0-XN in the above
        List<Class<?>> prefixArgs = getter.type().parameterList();
        // the N in X0-XN
        int numPrArgs = prefixArgs.size();

        // T in the above
        Class<?> propertyType = setter.type().parameterType(numPrArgs);
        if (propertyType.isPrimitive()) {
            // special case for primitives (because of serialized nulls -.-)
            return bindValueReaderPrim(prefixArgs, reader, setter);
        }

        // NBT in the above
        Class<?> nbtClass = reader.type().parameterType(0);

        // check if the NBT is a serialized null
        MethodHandle isSerNull = MethodHandles.dropArguments(NBTSerializerMethods.IS_SER_NULL, 0, prefixArgs);

        // make a handle that invokes the setter with null for T, ignoring the NBT
        MethodHandle setNull = MethodHandles.insertArguments(setter, numPrArgs, (Object) null);
        setNull = MethodHandles.dropArguments(setNull, numPrArgs, NBTBase.class);

        // make a handle that checks if the NBT has the correct ID and is not null (key is not present in NBTTagCompound)
        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL, 0, prefixArgs);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_ID, 1, nbtID), 0, prefixArgs);
        }

        // make a handle that takes all the prefix args + NBT and then calls the setter with the result of reader
        MethodHandle readSet = MethodHandles.filterArguments(setter, numPrArgs, reader.asType(methodType(propertyType, NBTBase.class)));

        // make a handle that does nothing but drop all the arguments, for the else branch
        List<Class<?>> allArgs = Lists.newArrayList(prefixArgs);
        allArgs.add(NBTBase.class);
        MethodHandle doNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, allArgs);

        // make a handle that invokes the setter if the NBT has the correct ID
        MethodHandle nonNullSet = MethodHandles.guardWithTest(isValid, readSet, doNothing);

        // make a handle that will either invoke the setter with null or try to read the NBT
        return MethodHandles.guardWithTest(isSerNull, setNull, nonNullSet);
    }

    private static MethodHandle bindValueReaderPrim(List<Class<?>> prefixArgs, MethodHandle reader, MethodHandle setter) {
        Class<?> propertyType = setter.type().parameterType(1);
        Class<?> nbtClass = reader.type().parameterType(0);

        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL_AND_NOT_SERNULL, 0, prefixArgs);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_NONNULL_AND_ID, 1, nbtID), 0, prefixArgs);
        }

        MethodHandle readSet = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(propertyType, NBTBase.class)));

        List<Class<?>> allArgs = Lists.newArrayList(prefixArgs);
        allArgs.add(NBTBase.class);
        MethodHandle doNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, allArgs);

        return MethodHandles.guardWithTest(isValid, readSet, doNothing);
    }

    private static MethodHandle bindContentReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        // T being the type of the property
        // X0-Xn being the data types needed to access the property via getter and setter
        //       (in case of a simple "field in a class" this is just a reference of that class)
        //       these might be no types at all
        //
        // S being the type that the reader handles (must be assignable from T)
        // NBT being the NBT type that the reader handles
        //
        // reader has type (NBT, S) => void
        // setter has type (X0, ..., XN, T) => void
        // getter has type (X0, ..., XN) => T
        //
        // we produce a method like this:
        // void read(X0 x0, ..., XN xn, NBTBase nbt) {
        //     if (nbt != <serialized null> && nbt instanceof NBT) {
        //         reader(getter(x0, ..., xn), (NBT) nbt);
        //     }
        // }
        //
        // the conditions are optimized as much as possible

        List<Class<?>> prefixArgs = getter.type().parameterList();

        Class<?> propertyType = getter.type().returnType();
        Class<?> nbtClass = reader.type().parameterType(1);

        MethodHandle adaptedRead = reader.asType(methodType(void.class, propertyType, NBTBase.class));
        MethodHandle doRead = MethodHandles.filterArguments(adaptedRead, 0, getter);

        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(NBTSerializerMethods.IS_NONNULL_AND_NOT_SERNULL, 0, prefixArgs);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(NBTSerializerMethods.IS_NONNULL_AND_ID, 1, nbtID), 0, prefixArgs);
        }

        List<Class<?>> allArgs = Lists.newArrayList(prefixArgs);
        allArgs.add(NBTBase.class);

        MethodHandle adaptedDoNothing = MethodHandles.dropArguments(NBTSerializerMethods.DO_NOTHING, 0, allArgs);
        return MethodHandles.guardWithTest(isValid, doRead, adaptedDoNothing);
    }

    static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
        NBTSerializers.validateGetterSetter(getter, setter);
        // T being the type of the property
        // X0-Xn being the data types needed to access the property via getter and setter
        //       (in case of a simple "field in a class" this is just a reference of that class)
        //       these might be no types at all
        //
        // S being the type that the reader handles (must be assignable from T)
        // NBT being the NBT type that the reader handles
        //
        // writer has type (S) => NBT
        // setter has type (X0, ..., XN, T) => void
        // getter has type (X0, ..., XN) => T
        //
        // we produce a method like this:
        // NBTBase write(X0 x0, ..., XN xn) {
        //     T t = getter(x0, ..., xn);
        //     if (t == null) {
        //         return serializedNull();
        //     } else {
        //         return writer(t);
        //     }

        Class<?> propertyType = getter.type().returnType();
        List<Class<?>> prefixArgs = getter.type().parameterList();

        MethodHandle serNul = MethodHandles.dropArguments(NBTSerializerMethods.SERIALIZED_NULL, 0, propertyType);
        MethodHandle adaptedWriter = writer.asType(methodType(NBTBase.class, propertyType));
        MethodHandle isNull = NBTSerializerMethods.IS_NULL.asType(methodType(boolean.class, propertyType));

        MethodHandle nullSafeWriter = MethodHandles.guardWithTest(isNull, serNul, adaptedWriter);
        nullSafeWriter = MethodHandles.dropArguments(nullSafeWriter, 1, prefixArgs);

        return MethodHandles.foldArguments(nullSafeWriter, getter);
    }

}