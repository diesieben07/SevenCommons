package de.take_weiland.mods.commons.nbt;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.nbt.NBTBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * <p>Registry for NBT serializers to be used with {@link de.take_weiland.mods.commons.nbt.ToNbt @ToNbt}.</p>
 * <p>A Serializer consists of two Methods: A serializer and a deserializer. These are represented by MethodHandles.</p>
 * @see de.take_weiland.mods.commons.nbt.NBTSerializerFactory
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class NBTSerializers {

    private static Multimap<Class<?>, NBTSerializerFactory> factories = ArrayListMultimap.create();

    static {
        SevenCommons.registerPostInitCallback(new Runnable() {
            @Override
            public void run() {
                freeze();
            }
        });
    }

    static synchronized void freeze() {
        factories = ImmutableMultimap.copyOf(factories);
    }

    private static boolean isFrozen() {
       return factories instanceof ImmutableMultimap;
    }

    /**
     * <p>Register a new NBTSerializerFactory.</p>
     * <p>The factory will be queried for properties of type {@code T} or any subtypes of {@code T}.
     * Any additional filtering is on behalf of the factory.</p>
     * @param clazz the base class
     * @param factory the factory
     */
    public static synchronized <T> void register(Class<T> clazz, NBTSerializerFactory factory) {
        checkState(!isFrozen(), "Register NBTSerializers before postInit");
        factories.put(clazz, factory);
    }

    /**
     * <p>Register a reader and writer MethodHandle.</p>
     * <p>The filter must have the exact type <tt>(Class&lt;?&gt;)->boolean</tt> or <tt>(TypeSpecification&lt;?&gt;)->boolean</tt>.</p>
     * <p>The reader and writer must be valid NBT serializers as defined by
     * {@link #bindReader(java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle) bindReader}
     * and {@link #bindWriter(java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle) bindWriter}.</p>
     * <p>The filter will be queried for properties of type {@code T} or any subtype of {@code T}. If it returns true, {@code reader}
     * and {@code writer} will be bound to the getter and setter using {@code bindReader} and {@code bindWriter}.</p>
     * @param clazz the base class
     * @param filter the filter
     * @param reader the reader
     * @param writer the writer
     */
    public static <T> void register(Class<T> clazz, MethodHandle filter, MethodHandle reader, MethodHandle writer) {
        validateReader(reader);
        validateWriter(writer);
        filter = validateFilter(filter);

        register(clazz, new MethodHandleBasedFactory(filter, reader, writer));
    }

    /**
     * <p>Equivalent to
     * {@link #register(Class, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle, java.lang.invoke.MethodHandle)}
     * with a MethodHandle equivalent to {@code (Class c) => return clazz == c} as the filter.</p>
     * @param clazz the base class
     * @param reader the reader
     * @param writer the writer
     */
    public static void register(Class<?> clazz, MethodHandle reader, MethodHandle writer) {
        MethodHandle filter = NBTSerializerMethods.CLASS_EQUAL.bindTo(clazz);
        register(clazz, filter, reader, writer);
    }

    /**
     * <p>Bind a reading MethodHandle to the given getter and setter.</p>
     * <p>Given two types {@code C} and {@code T} the getter must have type {@code (C)->T} and the setter must have
     * type {@code (C, T)->void}.</p>
     * <p>If the reader's return type is {@code void} it must have a type that is convertible to {@code (T, NBTBase)->void}
     * by the rules of {@link java.lang.invoke.MethodHandle#asType(java.lang.invoke.MethodType) MethodHandle.asType},
     * otherwise it must have a type that is convertible to {@code (NBTBase)->T} by the rules of {@code MethodHandle.asType}.
     * In either of those cases the last argument of the reader must be {@code NBTBase} or a subtype.</p>
     * <p>This method will in both cases produce a MethodHandle of the exact type {@code (C, NBTBase)->void}.</p>
     * @param reader the reader
     * @param getter the getter
     * @param setter the setter
     * @return a MethodHandle
     */
    public static MethodHandle bindReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        validateReader(reader);
        return MethodHandleBasedFactory.bindReader(reader, getter, setter);
    }

    /**
     * <p>Bind a writing MethodHandle to the given getter and setter.</p>
     * <p>Given two types {@code C} and {@code T} the getter must have type {@code (C)->T} and the setter must have
     * type {@code (C, T)->void}.</p>
     * <p>The writer's type must be convertible to {@code (T)->NBTBase} by the rules of
     * {@link java.lang.invoke.MethodHandle#asType(java.lang.invoke.MethodType) MethodHandle.asType}.</p>
     * <p>This method will produce a MethodHandle of the exact type {@code (C)->NBTBase}.</p>
     * @param writer the writer
     * @param getter the getter
     * @param setter the setter
     * @return a MethodHandle
     */
    public static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
        validateWriter(writer);
        return MethodHandleBasedFactory.bindWriter(writer, getter, setter);
    }

    /**
     * <p>Create a MethodHandle that can be used to read the property specified by the given {@code TypeSpecification},
     * {@code getter} and {@code setter} from NBT.</p>
     * <p>Given two types {@code C} and {@code T} the getter must have type {@code (C)->T} and the setter must have
     * type {@code (C, T)->void}.</p>
     * <p>The type of the resulting MethodHandle will then have the exact type {@code (C, NBTBase)->void}.</p>
     * @param spec the TypeSpecification
     * @param getter the getter
     * @param setter the setter
     * @return a MethodHandle
     */
    public static MethodHandle makeReader(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        return findMH(spec, getter, setter, false);
    }

    /**
     * <p>Create a MethodHandle that can be used to write the property specified by the given {@code TypeSpecification},
     * {@code getter} and {@code setter} to NBT.</p>
     * <p>Given two types {@code C} and {@code T} the getter must have type {@code (C)->T} and the setter must have
     * type {@code (C, T)->void}.</p>
     * <p>The type of the resulting MethodHandle will then have the exact type {@code (C)->NBTBase}.</p>
     * @param spec the TypeSpecification
     * @param getter the getter
     * @param setter the setter
     * @return a MethodHandle
     */
    public static MethodHandle makeWriter(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        return findMH(spec, getter, setter, true);
    }

    private static MethodHandle findMH(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, boolean writer) {
        checkState(isFrozen(), "Don't query NBTSerializers before postInit");
        validateGetterSetter(getter, setter);

        List<Class<?>> prefixArgs = getter.type().parameterList();

        Class<?> rawType = spec.getRawType();
        Iterable<Class<?>> hierarchy = JavaUtils.hierarchy(rawType, JavaUtils.Interfaces.INCLUDE);
        if (rawType.isInterface()) {
            hierarchy = Iterables.concat(hierarchy, ImmutableList.of(Object.class));
        }

        for (Class<?> clazz : hierarchy) {
            MethodHandle result = null;
            for (NBTSerializerFactory factory : factories.get(clazz)) {
                MethodHandle applied = writer ? factory.makeWriter(spec, getter, setter) : factory.makeReader(spec, getter, setter);

                if (applied != null) {
                    boolean valid;
                    if (writer) {
                        valid = applied.type().returnType() == NBTBase.class && applied.type().parameterList().equals(prefixArgs);
                    } else {
                        valid = applied.type().returnType() == void.class
                                && applied.type().parameterList().subList(0, applied.type().parameterCount() - 1).equals(prefixArgs)
                                && applied.type().parameterType(applied.type().parameterCount() - 1) == NBTBase.class;
                    }
                    checkArgument(valid, "Factory " + factory + " produced invalid " + (writer ? "writer" : "reader"));
                }

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

    private static MethodHandle validateFilter(MethodHandle filter) {
        checkArgument(filter.type().returnType() == boolean.class, "filter must return boolean");
        checkArgument(filter.type().parameterCount() == 1, "filter must take 1 argument");
        if (filter.type().parameterType(0) == Class.class) {
            filter = MethodHandles.filterArguments(filter, 0, NBTSerializerMethods.GET_RAW_TYPE);
        } else {
            checkArgument(filter.type().parameterType(0) == TypeSpecification.class, "filter must take Class<?> or TypeSpecification");
        }
        return filter;
    }

    static void validateGetterSetter(MethodHandle getter, MethodHandle setter) {
        MethodType getterType = getter.type();
        MethodType setterType = setter.type();
        checkArgument(getterType.returnType() != void.class, "Getter must not return void");
        checkArgument(setterType.returnType() == void.class, "Setter must return void");
        checkArgument(setterType.parameterCount() >= 1, "setter must take at least 1 argument");
        checkArgument(getterType.returnType() == setterType.parameterType(setterType.parameterCount() - 1), "setter and getter must handle same type");

        List<Class<?>> setterPrArgs = setterType.parameterList().subList(0, setterType.parameterCount() - 1);
        List<Class<?>> getterPrArgs = getterType.parameterList();
        checkArgument(setterPrArgs.equals(getterPrArgs), "getter and setter prefix arguments must match exactly");
    }

    private NBTSerializers() { }

}
