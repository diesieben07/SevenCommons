package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class NBTSerializers {

    private static Multimap<Class<?>, NBTSerializerFactory> factories = ArrayListMultimap.create();

    public static void register(Class<?> clazz, MethodHandle reader, MethodHandle writer) {
        validateReader(reader);
        validateWriter(writer);

        register(clazz, new MethodHandleBasedFactory(reader, writer));
    }

    public static synchronized void register(Class<?> clazz, NBTSerializerFactory factory) {
        factories.put(clazz, factory);
    }

    public static MethodHandle bindReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        return MethodHandleBasedFactory.bindReader(reader, getter, setter);
    }

    public static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
        return MethodHandleBasedFactory.bindWriter(writer, getter, setter);
    }

    public static CallSite makeWriterCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, MethodType expectedType) {
        return makeCallSite(spec, getter, setter, expectedType, true);
    }

    public static CallSite makeReaderCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, MethodType expectedType) {
        return makeCallSite(spec, getter, setter, expectedType, false);
    }

    private static CallSite makeCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, MethodType expectedType, boolean writer) {
        MethodHandle result = findMH(spec, getter, setter, writer);
        if (!result.type().equals(expectedType)) {
            throw new IllegalStateException("Internal Error: Failed to produce correct CallSite for NBT serialization");
        }
        return new ConstantCallSite(result);
    }

    private static MethodHandle findMH(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, boolean writer) {
        for (Class<?> clazz : JavaUtils.hierarchy(spec.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
            MethodHandle result = null;
            for (NBTSerializerFactory factory : factories.get(clazz)) {
                MethodHandle applied = writer ? factory.makeWriter(spec, getter, setter) : factory.makeReader(spec, getter, setter);
                if (result == null) {
                    result = applied;
                } else if (applied != null) {
                    throw new IllegalStateException("Found multiple NBT serializers for " + spec);
                }
            }
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException("Could not find a suitable NBT serializer for " + spec);
    }

    private static void validateReader(MethodHandle reader) {
        MethodType type = reader.type();
        if (type.returnType() == void.class) {
            checkArgument(type.parameterCount() == 2, "void returning NBT reader must take 2 args");
        } else {
            checkArgument(type.parameterCount() == 1, "value returning NBT reader must take 1 arg");
        }

        checkArgument(NBTBase.class.isAssignableFrom(type.parameterType(type.parameterCount() - 1)), "NBT reader must take NBT as last argument");
    }

    private static void validateWriter(MethodHandle writer) {
        MethodType type = writer.type();
        checkArgument(NBTBase.class.isAssignableFrom(type.returnType()), "NBT writer must return NBT");
        checkArgument(type.parameterCount() == 1, "NBT writer must take 1 argument");
    }

    private static class MethodHandleBasedFactory implements NBTSerializerFactory {

        private final MethodHandle reader;
        private final MethodHandle writer;

        MethodHandleBasedFactory(MethodHandle reader, MethodHandle writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
            return bindReader(reader, getter, setter);
        }

        static MethodHandle bindReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
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
                isValid = MethodHandles.dropArguments(IS_NONNULL_AND_NOT_SERNULL, 0, propertyHolder);
            } else {
                int nbtID = NBT.Tag.byClass(nbtClass).id();
                isValid = MethodHandles.dropArguments(MethodHandles.insertArguments(IS_NONNULL_AND_ID, 1, nbtID), 0, propertyHolder);
            }

            MethodHandle adaptedDoNothing = MethodHandles.dropArguments(DO_NOTHING, 0, propertyHolder, NBTBase.class);
            return MethodHandles.guardWithTest(isValid, doRead, adaptedDoNothing);
        }

        @Override
        public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
            return bindWriter(writer, getter, setter);
        }

        static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
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
    }

    static final MethodHandle SERIALIZED_NULL;
    static final MethodHandle IS_NULL;
    static final MethodHandle IS_SER_NULL;
    static final MethodHandle DO_NOTHING;
    static final MethodHandle IS_NONNULL;
    static final MethodHandle IS_ID;
    static final MethodHandle IS_NONNULL_AND_ID;
    static final MethodHandle IS_NONNULL_AND_NOT_SERNULL;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            SERIALIZED_NULL = lookup.findStatic(NBTData.class, "serializedNull", methodType(NBTBase.class));
            IS_NULL = lookup.findStatic(NBTSerializers.class, "isNull", methodType(boolean.class, Object.class));

            IS_SER_NULL = lookup.findStatic(NBTSerializers.class, "isRealSerializedNull", methodType(boolean.class, NBTBase.class));
            DO_NOTHING = MethodHandles.constant(Void.class, null).asType(methodType(void.class));
            IS_NONNULL = lookup.findStatic(NBTSerializers.class, "isNonNull", methodType(boolean.class, NBTBase.class));
            IS_ID = lookup.findStatic(NBTSerializers.class, "isDesiredID", methodType(boolean.class, NBTBase.class, int.class));
            IS_NONNULL_AND_ID = lookup.findStatic(NBTSerializers.class, "isNonnullAndID", methodType(boolean.class, NBTBase.class, int.class));
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

    private NBTSerializers() { }

}
