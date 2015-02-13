package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.nbt.NBTBase;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class NBTSerializers {

    private static Multimap<Class<?>, NBTSerializerFactory> factories = ArrayListMultimap.create();

    public static void registerWithTypeSpecFilter(Class<?> clazz, Predicate<TypeSpecification<?>> predicate, MethodHandle reader, MethodHandle writer) {
        MethodHandle filter = NBTSerializerMethods.PREDICATE_APPLY.bindTo(predicate).asType(methodType(boolean.class, TypeSpecification.class));
        register(clazz, filter, reader, writer);
    }

    public static void registerWithClassFilter(Class<?> clazz, Predicate<Class<?>> predicate, MethodHandle reader, MethodHandle writer) {
        MethodHandle filter = NBTSerializerMethods.PREDICATE_APPLY.bindTo(predicate).asType(methodType(boolean.class, Class.class));
        register(clazz, filter, reader, writer);
    }

    /**
     * <p>Register a MethodHandle </p>
     * @param clazz
     * @param reader
     * @param writer
     */
    public static void register(Class<?> clazz, MethodHandle reader, MethodHandle writer) {
        MethodHandle filter = NBTSerializerMethods.EQUAL.bindTo(clazz).asType(methodType(boolean.class, Class.class));
        register(clazz, filter, reader, writer);
    }

    /**
     * <p>Register a reader and writer MethodHandle.</p>
     * <p>The filter must have the exact type <tt>(Class&lt;?&gt;):boolean</tt> or <tt>(TypeSpecification&lt;?&gt;):boolean</tt>.</p>
     * <p></p>
     * @param clazz
     * @param filter
     * @param reader
     * @param writer
     */
    public static void register(Class<?> clazz, MethodHandle filter, MethodHandle reader, MethodHandle writer) {
        validateReader(reader);
        validateWriter(writer);

        register(clazz, new MethodHandleBasedFactory(filter, reader, writer));
    }

    /**
     * <p>Register a new NBTSerializerFactory. This factory will be queried for types of the given class or any superclasses and interfaces implemented by it directly
     * or indirectly. Any filtering is on behalf of the factory.</p>
     * @param clazz the base class
     * @param factory the factory
     */
    public static synchronized void register(Class<?> clazz, NBTSerializerFactory factory) {
        factories.put(clazz, factory);
    }

    public static MethodHandle bindReader(MethodHandle reader, MethodHandle getter, MethodHandle setter) {
        validateReader(reader);
        return MethodHandleBasedFactory.bindReader(reader, getter, setter);
    }

    public static MethodHandle bindWriter(MethodHandle writer, MethodHandle getter, MethodHandle setter) {
        validateWriter(writer);
        return MethodHandleBasedFactory.bindWriter(writer, getter, setter);
    }

    public static CallSite makeWriterCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        return makeCallSite(spec, getter, setter, true);
    }

    public static CallSite makeReaderCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        return makeCallSite(spec, getter, setter, false);
    }

    private static CallSite makeCallSite(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, boolean writer) {
        validateGetterSetter(getter, setter);

        MethodHandle result = findMH(spec, getter, setter, writer);
        return new ConstantCallSite(result);
    }

    private static MethodHandle findMH(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, boolean writer) {
        MethodType expectedType = writer ? methodType(NBTBase.class, getter.type().parameterType(0)) : methodType(void.class, getter.type().parameterType(0), NBTBase.class);

        for (Class<?> clazz : JavaUtils.hierarchy(spec.getRawType(), JavaUtils.Interfaces.INCLUDE)) {
            MethodHandle result = null;
            for (NBTSerializerFactory factory : factories.get(clazz)) {
                MethodHandle applied = writer ? factory.makeWriter(spec, getter, setter) : factory.makeReader(spec, getter, setter);

                if (applied != null) {
                    checkArgument(applied.type().equals(expectedType), "Factory " + factory + " produced invalid " + (writer ? "writer" : "reader"));
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

    static void validateGetterSetter(MethodHandle getter, MethodHandle setter) {
        MethodType getterType = getter.type();
        MethodType setterType = setter.type();
        checkArgument(getterType.returnType() != void.class, "Getter must not return void");
        checkArgument(getterType.parameterCount() == 1, "getter must take 1 argument");

        checkArgument(setterType.returnType() == void.class, "Setter must return void");
        checkArgument(setterType.parameterCount() == 2, "setter must take 2 arguments");

        checkArgument(getterType.returnType() == setterType.parameterType(1), "setter and getter must handle same type");
        checkArgument(getterType.parameterType(0) == setterType.parameterType(0), "setter and getter must take same instance type");
    }

    private NBTSerializers() { }

}
