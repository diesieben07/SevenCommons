package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public class DirectReaderFactory implements NBTReaderFactory {

    private final MethodHandle reader;

    public DirectReaderFactory(MethodHandle reader) {
        this.reader = reader;
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        if (reader.type().returnType() == void.class) {
            return makeContentReader(typeSpec, getter, setter);
        } else {
            return makeValueReader(typeSpec, getter, setter);
        }
    }

    private MethodHandle makeValueReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
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

        Class<?> propertyHolder = setter.type().parameterType(0);
        Class<?> propertyType = setter.type().parameterType(1);
        Class<?> nbtClass = reader.type().parameterType(0);

        MethodHandle setNull = MethodHandles.insertArguments(setter, 1, (Object) null);
        MethodHandle isSerNull = MethodHandles.dropArguments(IS_SER_NULL, 0, propertyHolder);
        MethodHandle isValid;
        if (nbtClass == NBTBase.class) {
            isValid = MethodHandles.dropArguments(IS_NONNULL, 0, propertyHolder);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(IS_ID, 1, nbtID), 0, propertyHolder);
        }
        MethodHandle readSet = MethodHandles.filterArguments(setter, 1, reader.asType(methodType(propertyType, NBTBase.class)));
        MethodHandle adaptedNothing = MethodHandles.dropArguments(DO_NOTHING, 0, propertyHolder, NBTBase.class);
        MethodHandle nonNullSet = MethodHandles.guardWithTest(isValid, readSet, adaptedNothing);

        return MethodHandles.guardWithTest(isSerNull, setNull, nonNullSet);
    }

    private MethodHandle makeContentReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
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
            isValid = MethodHandles.dropArguments(IS_NONNULL_AND_NOT_SERNULL, 0, propertyHolder);
        } else {
            int nbtID = NBT.Tag.byClass(nbtClass).id();
            isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(IS_NONNULL_AND_ID, 1, nbtID), 0, propertyHolder);
        }

        MethodHandle adaptedDoNothing = MethodHandles.dropArguments(DO_NOTHING, 0, propertyHolder, NBTBase.class);
        return MethodHandles.guardWithTest(isValid, doRead, adaptedDoNothing);
    }

    private static final MethodHandle IS_SER_NULL;
    private static final MethodHandle DO_NOTHING;
    private static final MethodHandle IS_NONNULL;
    private static final MethodHandle IS_ID;
    private static final MethodHandle IS_NONNULL_AND_ID;
    private static final MethodHandle IS_NONNULL_AND_NOT_SERNULL;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            IS_SER_NULL = lookup.findStatic(DirectReaderFactory.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
            IS_NONNULL = lookup.findStatic(DirectReaderFactory.class, "isNonNull", methodType(boolean.class, NBTBase.class));
            IS_ID = lookup.findStatic(DirectReaderFactory.class, "isDesiredID", methodType(boolean.class, NBTBase.class, int.class));
            IS_NONNULL_AND_ID = lookup.findStatic(DirectReaderFactory.class, "isNonnullAndID", methodType(boolean.class, NBTBase.class, int.class));
            IS_NONNULL_AND_NOT_SERNULL = lookup.findStatic(DirectReaderFactory.class, "isNonNullAndNotSerNull", methodType(boolean.class, NBTBase.class));
        } catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
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
