package de.take_weiland.mods.commons.serialize;

import com.google.common.base.Throwables;
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
public final class MethodPair {

    private final Class<?> typeClass;

    private final MethodHandle reader;
    private final MethodHandle writer;

    public MethodPair(MethodHandle reader, MethodHandle writer) {
        this.reader = checkAndSecureReader(reader);
        this.writer = checkAndSecureWriter(writer);

        typeClass = findTypeClass(reader, writer);
    }

    /**
     * <p>Make a MethodHandle that reads the given property (specified through get and set) from NBT.</p>
     * <p>With C being the class containing the property and T being the type of the property the following
     * must apply:</p>
     * <ul>
     *     <li>get must have type (C) => T</li>
     *     <li>set must have type (C, T) => void</li>
     * </ul>
     * <p>The resulting MethodHandle will then have type (NBTBase, C) => void</p>
     * @param get the getter
     * @param set the setter
     * @return a MethodHandle
     */
    public MethodHandle makeReader(MethodHandle get, MethodHandle set) {
        MethodHandle result;

        // adapt get and set for our type
        get = adaptGet(get);
        set = adaptSet(set);

        if (reader.type().returnType() == void.class) {
            // reader has type (NBTBase, T) => void
            // result has type (NBTBase, C) => void
            result = MethodHandles.filterArguments(reader, 1, get);
        } else {
            // reader has type (NBTBase) => T
            // setWithNBT has type (C, NBTBase)
            MethodHandle setWithNBT = MethodHandles.filterArguments(set, 1, reader);

            // swap arguments
            MethodType newType = methodType(setWithNBT.type().returnType(), setWithNBT.type().parameterType(1), setWithNBT.type().parameterType(1));
            result = MethodHandles.permuteArguments(setWithNBT, newType, 1, 0);
        }

        return result;
    }

    /**
     * <p>Returns (C) => NBTBase</p>
     * @param get
     * @param set
     * @return
     */
    public MethodHandle makeWriter(MethodHandle get, MethodHandle set) {
        MethodHandle result;

        get = adaptGet(get);

        return MethodHandles.filterArguments(writer, 0, get);
    }

    private MethodHandle adaptSet(MethodHandle set) {
        return set.asType(methodType(void.class, set.type().parameterType(0), typeClass));
    }

    private MethodHandle adaptGet(MethodHandle get) {
        return get.asType(methodType(typeClass, get.type().parameterType(1)));
    }

    private static MethodHandle checkAndSecureReader(MethodHandle reader) {
        MethodType type = reader.type();

        if (type.returnType() == void.class) {
            checkArgument(type.parameterCount() == 2, "void returning NBT reader must have 2 arguments");
        } else {
            checkArgument(type.parameterCount() == 1, "value-returning NBT reader must have 1 argument");
        }

        checkArgument(NBTBase.class.isAssignableFrom(type.parameterType(0)), "NBT reader must have NBT as first argument");


        return reader;
    }

    public static MethodHandle makeValueReader(MethodHandle reader, MethodHandle setter) {
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

    public static MethodHandle makeContentReader(MethodHandle reader, MethodHandle getter) {
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

    private static void checkValueReader(MethodHandle reader) {
        MethodType type = reader.type();
        checkArgument(type.returnType() != void.class, "value deserializer must not return void");
        checkArgument(type.parameterCount() == 1, "value deserializer must take 1 argument");
        checkArgument(type.parameterType(0).isAssignableFrom(NBTBase.class), "value deserializer must take NBT as 1st argument");
    }

    private static void checkSetter(MethodHandle setter) {
        MethodType setterType = setter.type();
        checkArgument(setterType.returnType() == void.class, "Setter must return void");
        checkArgument(setterType.parameterCount() == 2, "Setter must take 2 arguments");
        checkArgument(!setterType.parameterType(0).isPrimitive(), "Setter argument 0 must be reference");
    }

    private static MethodHandle checkAndSecureWriter(MethodHandle writer) {
        MethodType type = writer.type();

        checkArgument(NBTBase.class.isAssignableFrom(type.returnType()), "NBT writer must return NBTBase or subclass");
        checkArgument(type.parameterCount() == 1, "NBTWriter must take one argument");

        return writer;
    }

    private static final MethodHandle IS_SERIALIZED_NULL;
    private static final MethodHandle CHECK_NBT_ID;
    private static final MethodHandle CHECK_VALID_COMPOUND;
    private static final MethodHandle DO_NOTHING;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            IS_SERIALIZED_NULL = lookup.findStatic(MethodPair.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            CHECK_NBT_ID = lookup.findStatic(MethodPair.class, "checkNBTId", methodType(boolean.class, NBTBase.class, int.class));
            CHECK_VALID_COMPOUND = lookup.findStatic(MethodPair.class, "checkValidCompound", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
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
}
