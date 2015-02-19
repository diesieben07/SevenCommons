package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ListIterator;

import static de.take_weiland.mods.commons.asm.MCPNames.*;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class BuiltinSerializers implements NBTSerializerFactory {

    private static final MethodHandle READ_BOOL, WRITE_BOOL,
        READ_BYTE, WRITE_BYTE,
        READ_CHAR, WRITE_CHAR,
        READ_SHORT, WRITE_SHORT,
        READ_INT, WRITE_INT,
        READ_LONG, WRITE_LONG,
        READ_FLOAT, WRITE_FLOAT,
        READ_DOUBLE, WRITE_DOUBLE,
        READ_STRING, WRITE_STRING;

    private static final MethodHandle LIST_WRITER_HELPER;
    private static final MethodHandle LIST_READER_HELPER;
    private static final MethodHandle LIST_ITER_SET;
    private static final MethodHandle LIST_ADD_OR_SET;
    private static final MethodHandle LIST_SAFE_GET;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            READ_BYTE = lookup.findGetter(NBTTagByte.class, field(F_NBT_BYTE_DATA), byte.class);
            WRITE_BYTE = lookup.findConstructor(NBTTagByte.class, methodType(void.class, String.class, byte.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, byte.class));

            READ_BOOL = MethodHandles.explicitCastArguments(READ_BYTE, methodType(boolean.class, NBTTagByte.class));
            WRITE_BOOL = MethodHandles.explicitCastArguments(WRITE_BYTE, methodType(NBTBase.class, boolean.class));

            READ_SHORT = lookup.findGetter(NBTTagShort.class, field(F_NBT_SHORT_DATA), short.class);
            WRITE_SHORT = lookup.findConstructor(NBTTagShort.class, methodType(void.class, String.class, short.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, short.class));

            READ_CHAR = MethodHandles.explicitCastArguments(READ_SHORT, methodType(char.class, NBTTagShort.class));
            WRITE_CHAR = MethodHandles.explicitCastArguments(WRITE_SHORT, methodType(NBTBase.class, char.class));

            READ_INT = lookup.findGetter(NBTTagInt.class, field(F_NBT_INT_DATA), int.class);
            WRITE_INT = lookup.findConstructor(NBTTagInt.class, methodType(void.class, String.class, int.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, int.class));

            READ_LONG = lookup.findGetter(NBTTagLong.class, field(F_NBT_LONG_DATA), long.class);
            WRITE_LONG = lookup.findConstructor(NBTTagLong.class, methodType(void.class, String.class, long.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, long.class));

            READ_FLOAT = lookup.findGetter(NBTTagFloat.class, field(F_NBT_FLOAT_DATA), float.class);
            WRITE_FLOAT = lookup.findConstructor(NBTTagFloat.class, methodType(void.class, String.class, float.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, float.class));

            READ_DOUBLE = lookup.findGetter(NBTTagDouble.class, field(F_NBT_DOUBLE_DATA), double.class);
            WRITE_DOUBLE = lookup.findConstructor(NBTTagDouble.class, methodType(void.class, String.class, double.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, double.class));

            READ_STRING = lookup.findGetter(NBTTagString.class, field(F_NBT_STRING_DATA), String.class);
            WRITE_STRING = lookup.findConstructor(NBTTagString.class, methodType(void.class, String.class, String.class))
                    .bindTo("")
                    .asType(methodType(NBTBase.class, String.class));

            LIST_WRITER_HELPER = lookup.findStatic(BuiltinSerializers.class, "listWriterHelper", methodType(NBTBase.class, MethodHandle.class, List.class));
            LIST_ITER_SET = lookup.findVirtual(ListIterator.class, "set", methodType(void.class, Object.class));
            LIST_ADD_OR_SET = lookup.findStatic(BuiltinSerializers.class, "listAddOrSet", methodType(void.class, List.class, int.class, Object.class));
            LIST_SAFE_GET = lookup.findStatic(BuiltinSerializers.class, "listSafeGet", methodType(Object.class, List.class, int.class));
            LIST_READER_HELPER = lookup.findStatic(BuiltinSerializers.class, "listReaderHelper", methodType(void.class, MethodHandle.class, List.class, NBTTagList.class));
        } catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public MethodHandle makeReader(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        Class<?> clazz = typeSpec.getRawType();
        MethodHandle reader = null;

        if (typeSpec.getDesiredMethod() != SerializationMethod.Method.CONTENTS) {
            boolean box = Primitives.isWrapperType(clazz);
            if (box) {
                clazz = Primitives.unwrap(clazz);
            }

            if (clazz == boolean.class) {
                reader = READ_BOOL;
            }
            if (clazz == byte.class) {
                reader = READ_BYTE;
            }
            if (clazz == short.class) {
                reader = READ_SHORT;
            }
            if (clazz == char.class) {
                reader = READ_CHAR;
            }
            if (clazz == int.class) {
                reader = READ_INT;
            }
            if (clazz == long.class) {
                reader = READ_LONG;
            }
            if (clazz == float.class) {
                reader = READ_FLOAT;
            }
            if (clazz == double.class) {
                reader = READ_DOUBLE;
            }
            if (clazz == String.class) {
                reader = READ_STRING;
            }

            if (box && reader != null) {
                reader = reader.asType(methodType(Primitives.wrap(clazz), reader.type().parameterType(0)));
            }
        }
        if (typeSpec.getDesiredMethod() != SerializationMethod.Method.VALUE) {
            if (clazz == List.class) {
                reader = makeListValueReader(typeSpec, getter, setter);
            }
        }
        if (reader == null) {
            return null;
        } else {
            return NBTSerializers.bindReader(reader, getter, setter);
        }
    }

    @Override
    public MethodHandle makeWriter(TypeSpecification<?> typeSpec, MethodHandle getter, MethodHandle setter) {
        Class<?> clazz = typeSpec.getRawType();

        MethodHandle writer = null;
        if (typeSpec.getDesiredMethod() != SerializationMethod.Method.CONTENTS) {
            boolean unbox = Primitives.isWrapperType(clazz);
            if (unbox) {
                clazz = Primitives.unwrap(clazz);
            }

            writer = null;
            if (clazz == boolean.class) {
                writer = WRITE_BOOL;
            }
            if (clazz == byte.class) {
                writer = WRITE_BYTE;
            }
            if (clazz == short.class) {
                writer = WRITE_SHORT;
            }
            if (clazz == char.class) {
                writer = WRITE_CHAR;
            }
            if (clazz == int.class) {
                writer = WRITE_INT;
            }
            if (clazz == long.class) {
                writer = WRITE_LONG;
            }
            if (clazz == float.class) {
                writer = WRITE_FLOAT;
            }
            if (clazz == double.class) {
                writer = WRITE_DOUBLE;
            }
            if (clazz == String.class) {
                writer = WRITE_STRING;
            }

            if (unbox && writer != null) {
                writer = writer.asType(methodType(NBTBase.class, Primitives.wrap(clazz)));
            }
        }

        if (typeSpec.getDesiredMethod() != SerializationMethod.Method.VALUE) {
            if (clazz == List.class) {
                writer = makeListValueWriter(typeSpec, getter, setter);
            }
        }

        if (writer == null) {
            return null;
        } else {
            return NBTSerializers.bindWriter(writer, getter, setter);
        }
    }

    private static final Type ITER_TYPE = Iterable.class.getTypeParameters()[0];

    private static MethodHandle makeListValueWriter(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        TypeToken<?> listType = spec.getType().resolveType(ITER_TYPE);
        if (listType.isAssignableFrom(List.class)) {
            throw new UnsupportedOperationException("Nested lists not supported yet");
        }

        MethodHandle valGetter = MethodHandles.dropArguments(MethodHandles.identity(Object.class), 1, ListIterator.class);
        MethodHandle valSetter = MethodHandles.dropArguments(LIST_ITER_SET, 0, Object.class);

        TypeSpecification<?> valueSpec = spec.overwriteType(listType, SerializationMethod.Method.VALUE);
        MethodHandle valueWriter = NBTSerializers.makeWriter(valueSpec, valGetter, valSetter);
        return LIST_WRITER_HELPER.bindTo(valueWriter);
    }

    private static MethodHandle makeListValueReader(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter) {
        TypeToken<?> listType = spec.getType().resolveType(ITER_TYPE);
        if (listType.isAssignableFrom(List.class)) {
            throw new UnsupportedOperationException("Nested lists not supported yet");
        }

        MethodHandle valGetter = LIST_SAFE_GET;
        MethodHandle valSetter = LIST_ADD_OR_SET;
        TypeSpecification<?> valueSpec = spec.overwriteType(listType, SerializationMethod.Method.VALUE);
        MethodHandle valueReader = NBTSerializers.makeReader(valueSpec, valGetter, valSetter);
        return LIST_READER_HELPER.bindTo(valueReader);
    }

    private static void listReaderHelper(MethodHandle valueReader, List<?> list, NBTTagList nbt) throws Throwable {
        list.clear();
        int len = nbt.tagCount();
        for (int i = 0; i < len; i++) {
            valueReader.invokeExact((List<?>) list, (int) i, (NBTBase) nbt.tagAt(i));
        }
    }

    private static <T> void listAddOrSet(List<T> list, int idx, T o) {
        if (list.size() == idx) {
            list.add(o);
        } else {
            // handles potential IOOBE
            list.set(idx, o);
        }
    }

    private static Object listSafeGet(List<?> list, int i) {
        return i < list.size() ? list.get(i) : null;
    }

    private static NBTBase listWriterHelper(MethodHandle valueWriter, List<?> list) throws Throwable {
        NBTTagList nbt = new NBTTagList();
        ListIterator<?> it = list.listIterator();
        while (it.hasNext()) {
            Object o = it.next();
            nbt.appendTag((NBTBase) valueWriter.invokeExact((Object) o, (ListIterator<?>) it));
        }
        return nbt;
    }


}
