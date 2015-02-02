package de.take_weiland.mods.commons.serialize;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.nbt.ToNbt;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.Type;

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
     * @param actualType the type of the property (T in the above)
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

        MethodHandle nbtGuarded = guardNBTType(reader);

        return reader;
    }

    private static MethodHandle guardNBTType(MethodHandle reader, ToNbt.MissingAction missingAction) {
        Class<?> nbtClass = reader.type().parameterType(0);
        if (nbtClass == NBTBase.class) {
            return reader;
        }
        int nbtID = NBT.Tag.byClass(nbtClass).id();


        MethodHandle guarded;
        if (reader.type().returnType() == void.class) {
            // reader has type (NBTBase, T) => void
            Class<?> readerParam = reader.type().parameterType(1);
            MethodHandle onMissing = MethodHandles.dropArguments(DO_NOTHING, 0, NBTBase.class, readerParam);

            MethodHandle test;
            if (nbtID == NBT.TAG_COMPOUND) {
                test = CHECK_NBT_COMPOUND;
            } else {
                test = MethodHandles.insertArguments(CHECK_NBT_VALID, 1, nbtID);
            }

            reader = reader.asType(methodType(void.class, NBTBase.class, readerParam));
            guarded = MethodHandles.guardWithTest(test, reader, onMissing);
        } else {
            Class<?> readerParam = reader.type().returnType();

            MethodHandle test;
            MethodHandle onMissing;
            if (missingAction == ToNbt.MissingAction.IGNORE) {
                onMissing = MethodHandles.dropArguments()
            }

            reader = reader.asType(methodType(readerParam, NBTBase.class));
            guarded = MethodHandles.guardWithTest(test, reader, onMissing);
        }

        return guarded;
    }

    private static MethodHandle makeValueReader(MethodHandle reader, MethodHandle setter, ToNbt.MissingAction action) {
        checkValueReader(reader);

        Class<?> type = reader.type().returnType();

        checkSetter(setter);
        checkArgument(setter.type().parameterType(1).isAssignableFrom(type));

        MethodHandle doSet = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(type, NBTBase.class)));

        MethodHandle onMissing;

        if (action == ToNbt.MissingAction.DEFAULT) {
            MethodHandle setDefault = MethodHandles.insertArguments(setter, 1, getDefaultValue(type));
            onMissing = MethodHandles.dropArguments(setDefault, 1, NBTBase.class);
        } else {
            onMissing = MethodHandles.dropArguments(DO_NOTHING, 0, setter.type().parameterType(0), NBTBase.class);
        }

        MethodHandle onSerNull;
        if (type.isPrimitive()) {
            onSerNull = onMissing;
        } else {
            onSerNull = MethodHandles.insertArguments(setter, 1, (Object) null);
        }

        MethodHandle guard;
        if (reader.type().parameterType(0) == NBTBase.class) {
            guard =
        }
    }

    private static Object getDefaultValue(Class<?> clazz) {
        switch (Type.getType(clazz).getSort()) {
            case Type.BOOLEAN:
                return false;
            case Type.BYTE:
                return (byte) 0;
            case Type.SHORT:
                return (short) 0;
            case Type.INT:
                return 0;
            case Type.CHAR:
                return (char) 0;
            case Type.LONG:
                return 0L;
            case Type.FLOAT:
                return 0f;
            case Type.DOUBLE:
                return 0d;
            default:
                return null;
        }
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
    private static final MethodHandle IS_NBT_NONNULL;
    private static final MethodHandle CHECK_NBT_VALID;
    private static final MethodHandle CHECK_NBT_COMPOUND;
    private static final MethodHandle DO_NOTHING;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            IS_SERIALIZED_NULL = lookup.findStatic(MethodPair.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            IS_NBT_NONNULL = lookup.findStatic(MethodPair.class, "isNBTNonNull", methodType(boolean.class, NBTBase.class));
            CHECK_NBT_VALID = lookup.findStatic(MethodPair.class, "checkNBTId", methodType(boolean.class, NBTBase.class, int.class));
            CHECK_NBT_COMPOUND = lookup.findStatic(MethodPair.class, "checkNBTCompound", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    private static boolean checkNBTId(@Nullable NBTBase nbt, int desired) {
        return nbt != null && nbt.getId() == desired;
    }

    private static boolean isNBTNonNull(@Nullable NBTBase nbt) {
        return nbt != null;
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
    }
}
