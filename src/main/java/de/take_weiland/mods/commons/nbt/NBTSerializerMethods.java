package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
final class NBTSerializerMethods {

    static final MethodHandle GET_RAW_TYPE;
    static final MethodHandle SERIALIZED_NULL;
    static final MethodHandle IS_NULL;
    static final MethodHandle IS_SER_NULL;
    static final MethodHandle DO_NOTHING;
    static final MethodHandle IS_NONNULL;
    static final MethodHandle IS_ID;
    static final MethodHandle IS_NONNULL_AND_ID;
    static final MethodHandle IS_NONNULL_AND_NOT_SERNULL;
    static final MethodHandle EQUAL;
    static final MethodHandle PREDICATE_APPLY;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            GET_RAW_TYPE = lookup.findVirtual(TypeSpecification.class, "getRawType", methodType(Class.class));

            EQUAL = lookup.findStatic(Objects.class, "equals", methodType(boolean.class, Object.class, Object.class));

            PREDICATE_APPLY = lookup.findVirtual(Predicate.class, "apply", methodType(boolean.class, Object.class));

            SERIALIZED_NULL = lookup.findStatic(NBTData.class, "serializedNull", methodType(NBTBase.class));
            IS_NULL = lookup.findStatic(NBTSerializerMethods.class, "isNull", methodType(boolean.class, Object.class));
            IS_SER_NULL = lookup.findStatic(NBTSerializerMethods.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
            IS_NONNULL = lookup.findStatic(NBTSerializerMethods.class, "isNonNull", methodType(boolean.class, NBTBase.class));
            IS_ID = lookup.findStatic(NBTSerializerMethods.class, "isDesiredID", methodType(boolean.class, NBTBase.class, int.class));
            IS_NONNULL_AND_ID = lookup.findStatic(NBTSerializerMethods.class, "isNonnullAndID", methodType(boolean.class, NBTBase.class, int.class));
            IS_NONNULL_AND_NOT_SERNULL = lookup.findStatic(NBTSerializers.class, "isNonNullAndNotSerNull", methodType(boolean.class, NBTBase.class));
        } catch(ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
    }

    private static boolean isNull(@Nullable Object o) {
        return o == null;
    }

    private static boolean isRealSerializedNull(@Nullable NBTBase nbt) {
        return nbt != null && nbt.getId() == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NBTData.NULL_KEY) == NBTData.NULL;
    }

    private static boolean isNonNull(@Nullable NBTBase nbt) {
        return nbt != null;
    }

    private static boolean isDesiredID(@Nullable NBTBase nbt, int id) {
        return nbt != null && nbt.getId() == id;
    }

    private static boolean isNonnullAndID(@Nullable NBTBase nbt, int id) {
        if (nbt == null) {
            return false;
        }
        if (nbt.getId() != id) {
            return false;
        }
        return id != NBT.TAG_COMPOUND || ((NBTTagCompound) nbt).getByte(NBTData.NULL_KEY) != NBTData.NULL;
    }

    private static boolean isNonNullAndNotSerNull(@Nullable NBTBase nbt) {
        return nbt != null && (nbt.getId() != NBT.TAG_COMPOUND || ((NBTTagCompound) nbt).getByte(NBTData.NULL_KEY) == NBTData.NULL);
    }
}
