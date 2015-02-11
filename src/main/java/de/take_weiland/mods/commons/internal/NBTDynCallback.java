package de.take_weiland.mods.commons.internal;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.google.common.reflect.TypeToken;
import cpw.mods.fml.common.discovery.ASMDataTable;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.SerializerTypes;
import de.take_weiland.mods.commons.serialize.DirectNBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.TypeSpecification;
import net.minecraft.nbt.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.*;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.lang.reflect.Type;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class NBTDynCallback {

    public static CallSite makeNBTWrite(MethodHandles.Lookup foreignLookup, String name, MethodType type, int nameIsMethod, MethodHandle get, MethodHandle set) throws Throwable {
        Class<?> valueClass = findValueClass(get.type().returnType(), set.type().parameterType(1));
        Class<?> propertyHolderClass = foreignLookup.lookupClass();

        if (!type.equals(methodType(NBTBase.class, propertyHolderClass))) {
            throw new IllegalArgumentException("Invalid InDy method type!");
        }

        checkArgument(get.type().equals(methodType(valueClass, propertyHolderClass)), "invalid getter");
        checkArgument(set.type().equals(methodType(void.class, propertyHolderClass, valueClass)), "invalid setter");

        TypeSpecification<?> typeSpec;
        if (nameIsMethod != 0) {
            typeSpec = new MethodSpecImpl<>(propertyHolderClass.getDeclaredMethod(name), SerializationMethod.VALUE);
        } else {
            typeSpec = new FieldSpecImpl<>(propertyHolderClass.getDeclaredField(name), SerializationMethod.VALUE);
        }

        MethodHandle writer = findMethod(typeSpec, get, set, false);
        return new ConstantCallSite(writer);
    }

    private static Class<?> findValueClass(Class<?> a, Class<?> b) {
        if (a == b) {
            return a;
        }
        throw new IllegalArgumentException("getter and setter must have same type");
    }

    private static Map<Class<?>, NBTSerializerWrapper> allHandlers = Maps.newHashMap();

    private static MethodHandle findMethod(TypeSpecification<?> spec, MethodHandle getter, MethodHandle setter, boolean findReader) {
        NBTSerializerWrapper handler = allHandlers.get(spec.getRawType());
        MethodHandle result;
        if (handler == null) {
            result = null;
        } else {
            result = findReader ? handler.makeReader(spec, getter, setter) : handler.makeWriter(spec, getter, setter);
        }
        if (result == null) {
            throw new IllegalArgumentException("No NBT handler found for " + spec);
        }
        return result;
    }

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            MethodHandle writeString = lookup.findConstructor(NBTTagString.class, methodType(void.class, String.class, String.class))
                    .bindTo("");
            MethodHandle readString = lookup.findGetter(NBTTagString.class, MCPNames.field(MCPNames.F_NBT_STRING_DATA), String.class);
            allHandlers.put(String.class, new DirectNBTSerializer(readString, writeString));

            MethodHandle writeByte = lookup.findConstructor(NBTTagByte.class, methodType(void.class, String.class, byte.class))
                    .bindTo("");
            MethodHandle readByte = lookup.findGetter(NBTTagByte.class, MCPNames.field(MCPNames.F_NBT_BYTE_DATA), byte.class);
            allHandlers.put(byte.class, new DirectNBTSerializer(readByte, writeByte));

            MethodHandle writeBool = MethodHandles.explicitCastArguments(writeByte, methodType(NBTTagByte.class, boolean.class));
            MethodHandle readBool = MethodHandles.explicitCastArguments(readByte, methodType(boolean.class, NBTTagByte.class));
            allHandlers.put(boolean.class, new DirectNBTSerializer(readBool, writeBool));

            MethodHandle writeShort = lookup.findConstructor(NBTTagShort.class, methodType(void.class, String.class, short.class))
                    .bindTo("");
            MethodHandle readShort = lookup.findGetter(NBTTagShort.class, MCPNames.field(MCPNames.F_NBT_SHORT_DATA), short.class);
            allHandlers.put(short.class, new DirectNBTSerializer(readShort, writeShort));

            MethodHandle writeChar = MethodHandles.explicitCastArguments(writeShort, methodType(NBTTagShort.class, char.class));
            MethodHandle readChar = MethodHandles.explicitCastArguments(readShort, methodType(char.class, NBTTagShort.class));
            allHandlers.put(char.class, new DirectNBTSerializer(readChar, writeChar));

            doPrimRegister(lookup, NBTTagInt.class, int.class, MCPNames.F_NBT_INT_DATA);
            doPrimRegister(lookup, NBTTagLong.class, long.class, MCPNames.F_NBT_LONG_DATA);
            doPrimRegister(lookup, NBTTagFloat.class, float.class, MCPNames.F_NBT_FLOAT_DATA);
            doPrimRegister(lookup, NBTTagDouble.class, double.class, MCPNames.F_NBT_DOUBLE_DATA);
            doPrimRegister(lookup, NBTTagIntArray.class, int[].class, MCPNames.F_NBT_INT_ARR_DATA);
            doPrimRegister(lookup, NBTTagByteArray.class, byte[].class, MCPNames.F_NBT_BYTE_ARR_DATA);
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    private static void doPrimRegister(MethodHandles.Lookup lookup, Class<?> nbt, Class<?> prim, String field) throws Throwable {
        MethodHandle write = lookup.findConstructor(nbt, methodType(void.class, String.class, prim))
                .bindTo("");
        MethodHandle read = lookup.findGetter(nbt, MCPNames.field(field), prim);
        allHandlers.put(prim, new DirectNBTSerializer(read, write));
    }

    private static void init(ASMDataTable asmData) {
        Set<ASMDataTable.ASMData> all = asmData.getAll(NBTSerializer.class.getName());
        List<Pair<Method, MethodHandle>> readers = new ArrayList<>();
        List<Pair<Method, MethodHandle>> writers = new ArrayList<>();

        for (ASMDataTable.ASMData data : all) {
            Method m = getMethod(data);
            Pair<Method, MethodHandle> pair = null;
            try {
                pair = new ImmutablePair<>(m, publicLookup().unreflect(m));
            } catch (IllegalAccessException e) {
                throw new AssertionError("Impossible", e);
            }
            if (isWriter(m)) {
                writers.add(pair);
            } else if (isReader(m)) {
                readers.add(pair);
            } else {
                throw new IllegalStateException("@NBTSerializer on invalid method");
            }
        }
    }

    private static boolean isWriter(Method m) {
        return NBTBase.class.isAssignableFrom(m.getReturnType()) && m.getParameterTypes().length == 1;
    }

    private static boolean isReader(Method m) {
        Class<?>[] args = m.getParameterTypes();
        if (m.getReturnType() == void.class) {
            return args.length == 2 && NBTBase.class.isAssignableFrom(args[1]);
        } else {
            return args.length == 1 && NBTBase.class.isAssignableFrom(args[0]);
        }
    }

    private static Method getMethod(ASMDataTable.ASMData data) {
        Class<?> clazz;
        try {
            clazz = Class.forName(data.getClassName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Annotation in non-existent class?!");
        }
        String oName = data.getObjectName();
        int idx = oName.indexOf('(');
        String name = oName.substring(0, idx);
        String sig = oName.substring(idx);

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name) && org.objectweb.asm.Type.getMethodDescriptor(method).equals(sig)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalStateException("Annotation on non-existent method?!");
    }

    private static class GetSourceClassFunc implements Function<ASMDataTable.ASMData, Class<?>> {

        @Nullable
        @Override
        public Class<?> apply(ASMDataTable.ASMData input) {
            try {
                return Class.forName(input.getClassName());
            } catch (ClassNotFoundException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private static 

    private static void handleSerializer(ASMDataTable.ASMData data) {
        MethodHandle mh = asSerializerMethod(data);
        if (NBTBase.class.isAssignableFrom(mh.type().returnType())) {
                    newWriter(mh);
        } else if (method.getReturnType() == void.class) {

        }
    }


    private static MethodHandle asSerializerMethod(ASMDataTable.ASMData data) {
        try {
            Class<?> containingClass = Class.forName(data.getClassName());
            String nameAndSig = data.getObjectName();
            int idx = nameAndSig.indexOf('(');
            String name = nameAndSig.substring(0, idx);
            String sig = nameAndSig.substring(idx);

            org.objectweb.asm.Type[] args = org.objectweb.asm.Type.getArgumentTypes(sig);
            if (args.length == 0) {
                throw new IllegalStateException("@NBTSerializer method must take at least 1 argument");
            }
            Class<?>[] classArgs = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                classArgs[i] = ASMUtils.getClass(args[i]);
            }

            Method method = containingClass.getDeclaredMethod(name, classArgs);
            if (NBTBase.class.isAssignableFrom(method.getReturnType())) {
                // a writer
                if (classArgs.length != 1) {
                    throw new IllegalArgumentException("@NBTSerializer that returns NBT must take 1 argument");
                }
            } else if(method.getReturnType() == void.class) {
                // content reader
                if (classArgs.length != 2 || !NBTBase.class.isAssignableFrom(classArgs[1])) {
                    throw new IllegalArgumentException("@NBTSerializer that returns void must take 2 arguments: value and NBT");
                }
            } else {
                // value reader
                if (classArgs.length != 1 || !NBTBase.class.isAssignableFrom(classArgs[0])) {
                    throw new IllegalArgumentException("@NBTSerializer that returns a value must take 1 argument: NBT");
                }
            }
            method.setAccessible(true);
            return MethodHandles.publicLookup().unreflect(method);
        } catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
    }

    private static abstract class TypeSpecImpl<T, MEM extends Member & AnnotatedElement> implements TypeSpecification<T> {

        final MEM member;
        private final SerializationMethod method;
        private TypeToken<T> typeToken;

        TypeSpecImpl(MEM member, SerializationMethod method) {
            this.member = member;
            this.method = method;
        }

        @Override
        public TypeToken<T> getType() {
            if (typeToken == null) {
                //noinspection unchecked
                typeToken = (TypeToken<T>) TypeToken.of(genericType());
            }
            return typeToken;
        }

        abstract Type genericType();

        @Override
        public SerializationMethod getDesiredMethod() {
            return method;
        }

        @Override
        public boolean hasAnnotation(Class<? extends Annotation> annotation) {
            return member.isAnnotationPresent(annotation);
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
            return member.getAnnotation(annotationClass);
        }
    }

    private static final class FieldSpecImpl<T> extends TypeSpecImpl<T, Field> {

        FieldSpecImpl(Field member, SerializationMethod method) {
            super(member, method);
        }

        @Override
        Type genericType() {
            return member.getGenericType();
        }

        @Override
        public Class<? super T> getRawType() {
            //noinspection unchecked
            return (Class<? super T>) member.getType();
        }
    }

    private static final class MethodSpecImpl<T> extends TypeSpecImpl<T, Method> {

        MethodSpecImpl(Method member, SerializationMethod method) {
            super(member, method);
        }

        @Override
        Type genericType() {
            return member.getGenericReturnType();
        }

        @Override
        public Class<? super T> getRawType() {
            //noinspection unchecked
            return (Class<? super T>) member.getReturnType();
        }
    }

}
