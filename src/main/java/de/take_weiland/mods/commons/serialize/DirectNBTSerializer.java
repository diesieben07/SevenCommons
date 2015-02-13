package de.take_weiland.mods.commons.serialize;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.internal.NBTSerializerWrapper;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTData;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class DirectNBTSerializer extends NBTSerializerWrapper {

    private final Class<?> typeClass;

    private final MethodHandle reader;
    private final MethodHandle writer;

    public DirectNBTSerializer(MethodHandle reader, MethodHandle writer) {
        this.reader = checkReader(reader);
        this.writer = checkWriter(writer);

        typeClass = findTypeClass(reader, writer);
    }

    private static MethodHandle checkReader(MethodHandle reader) {
        MethodType type = reader.type();
        checkArgument(NBTBase.class.isAssignableFrom(type.parameterType(0)), "reader must take NBT as first argument");
        if (type.returnType() == void.class) {
            checkArgument(type.parameterCount() == 2, "void returning NBT reader must take 2 arguments");
        } else {
            checkArgument(type.parameterCount() == 1, "value returning NBT reader must take only NBT as argument");
        }
        return reader;
    }

    private static MethodHandle checkWriter(MethodHandle writer) {
        MethodType type = writer.type();
        checkArgument(NBTBase.class.isAssignableFrom(type.returnType()), "NBT writer must return NBT");
        checkArgument(type.parameterCount() == 1, "NBT writer must take 1 parameter");
        return writer;
    }

    private static Class<?> findTypeClass(MethodHandle reader, MethodHandle writer) {
        Class<?> writerType = writer.type().parameterType(0);
        Class<?> readerType;
        if (reader.type().returnType() == void.class) {
            readerType = reader.type().parameterType(1);
        } else {
            readerType = reader.type().returnType();
        }
        checkArgument(readerType == writerType, "NBT reader and writer must handle same type");
        return readerType;
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        return makeReader0(reader, getter, setter);
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        return makeWriter0(writer, getter);
    }

    public static MethodHandle makeReader0(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        if (reader.type().returnType() == void.class) {
            return makeContentReader(reader, getter);
        } else {
            return makeValueReader(reader, setter);
        }
    }

    private  static MethodHandle makeValueReader(MethodHandle reader, MethodHandle setter) {
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
        //     } else if (nbt instanceof NBT) { // includes null check
        //         setter(c, reader((NBT) nbt));
        //     }
        // }
        //
        // the conditions are optimized as possible

        Class<?> propertyHolderClass = setter.type().parameterType(0);
        Class<?> actualValueClass = setter.type().parameterType(1);

        int nbtID = NBT.Tag.byClass(reader.type().parameterType(0)).id();

        MethodHandle doNothing = MethodHandles.dropArguments(DO_NOTHING, 0, propertyHolderClass, NBTBase.class);
        MethodHandle isSerNull = MethodHandles.dropArguments(IS_SERIALIZED_NULL, 0, propertyHolderClass);
        MethodHandle setToNull = MethodHandles.dropArguments(MethodHandles.insertArguments(setter, 1, (Object) null), 1, NBTBase.class);
        MethodHandle isValidNBT = MethodHandles.dropArguments(MethodHandles.insertArguments(CHECK_NBT_ID, 1, nbtID), 0, propertyHolderClass);
        MethodHandle doSetAndRead = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(actualValueClass, NBTBase.class)));

        MethodHandle nonNullSet = MethodHandles.guardWithTest(isValidNBT, doSetAndRead, doNothing);
        return MethodHandles.guardWithTest(isSerNull, setToNull, nonNullSet);
    }

    private static MethodHandle makeContentReader(MethodHandle reader, MethodHandle getter) {
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
        // the condition is optimized to not use instanceof.


        Class<?> propertyHolderClass = getter.type().parameterType(0);
        Class<?> actualValueClass = getter.type().returnType();

        Class<?> nbtClass = reader.type().parameterType(0);

        MethodHandle doNothing = MethodHandles.dropArguments(DO_NOTHING, 0, propertyHolderClass, NBTBase.class);
        MethodHandle invReader = MethodHandles.permuteArguments(reader.asType(methodType(void.class, NBTBase.class, actualValueClass)), methodType(void.class, actualValueClass, NBTBase.class), 1, 0);
        MethodHandle doRead = MethodHandles.filterArguments(invReader, 0, getter);

        MethodHandle guarded;
        if (nbtClass == NBTBase.class) {
            MethodHandle isSerNull = MethodHandles.dropArguments(IS_SERIALIZED_NULL, 0, propertyHolderClass);
            guarded = MethodHandles.guardWithTest(isSerNull, doNothing, doRead);
        } else if (nbtClass == NBTTagCompound.class) {
            MethodHandle isValid = MethodHandles.dropArguments(CHECK_VALID_COMPOUND, 0, propertyHolderClass);
            guarded = MethodHandles.guardWithTest(isValid, doRead, doNothing);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            MethodHandle isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(CHECK_NBT_ID, 1, nbtID), 0, propertyHolderClass);
            guarded = MethodHandles.guardWithTest(isValid, doRead, doNothing);
        }
        return guarded;
    }

    public static MethodHandle makeWriter0(MethodHandle writer, MethodHandle getter) {
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

        Class<?> actualValueClass = getter.type().returnType();

        MethodHandle isNull = IS_REF_NULL.asType(methodType(boolean.class, actualValueClass));
        MethodHandle guarded = MethodHandles.guardWithTest(isNull, MethodHandles.dropArguments(SER_NULL, 0, actualValueClass), writer.asType(methodType(NBTBase.class, actualValueClass)));
        return MethodHandles.filterArguments(guarded, 0, getter);
    }

    private static final MethodHandle IS_SERIALIZED_NULL;
    private static final MethodHandle CHECK_NBT_ID;
    private static final MethodHandle CHECK_VALID_COMPOUND;
    private static final MethodHandle DO_NOTHING;
    private static final MethodHandle IS_REF_NULL;
    private static final Method Handle SER_NULL;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            IS_SERIALIZED_NULL = lookup.findStatic(DirectNBTSerializer.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            CHECK_NBT_ID = lookup.findStatic(DirectNBTSerializer.class, "checkNBTId", methodType(boolean.class, NBTBase.class, int.class));
            CHECK_VALID_COMPOUND = lookup.findStatic(DirectNBTSerializer.class, "checkValidCompound", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
            IS_REF_NULL = lookup.findStatic(DirectNBTSerializer.class, "isRefNull", methodType(boolean.class, Object.class));
            SER_NULL = lookup.findStatic(NBTData.class, "serializedNull", methodType(NBTBase.class));
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    private static boolean isRefNull(@Nullable Object o) {
        return o == null;
    }

    private static boolean checkValidCompound(@Nullable NBTBase nbt) {
        if (nbt == null) {
            return false;
        }
        if (nbt.getId() != NBT.TAG_COMPOUND) {
            return false;
        }
        NBTBase nullKey = ((NBTTagCompound) nbt).getTag(NBTData.NULL_KEY);
        return nullKey == null || nullKey.getId() != NBT.TAG_BYTE || ((NBTTagByte) nullKey).data != NBTData.NULL;
    }

    private static boolean checkNBTId(@Nullable NBTBase nbt, int desired) {
        return nbt != null && nbt.getId() == desired;
    }

    private static boolean isRealSerializedNull(@Nullable NBTBase nbt) {
        return nbt != null && nbt.getId() == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NBTData.NULL_KEY) == NBTData.NULL;
    }

    private static Object defaultValue(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return null;
        } else {
            if (clazz == boolean.class) {
                return false;
            } else {
                return 0;
            }
        }
    }

}
