package de.take_weiland.mods.commons.internal.tonbt;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.serialize.Property;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.List;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
final class BytecodeEmittingHandlerGenerator {

    private static final String SERIALIZER = "ser";
    private static final String GETTER = "get";
    private static final String SETTER = "set";

    private final DefaultHandlerFactory factory;
    private final Class<?> clazz;
    private ClassWriter cw;
    private String className;
    private String superName;
    private List<Property<?, ?>> properties;

    BytecodeEmittingHandlerGenerator(DefaultHandlerFactory factory, Class<?> clazz) {
        this.factory = factory;
        this.clazz = clazz;
    }

    Class<? extends ToNbtHandler> generateHandler() {
        properties = ToNbtFactories.getProperties(clazz);
        if (properties.isEmpty()) {
            return null;
        }

        newClassWriter();

        genFields();
        genCLInit();
        genWrite();
        genRead();

        return finish();
    }

    private void newClassWriter() {
        className = SCReflection.nextDynamicClassName(BytecodeEmittingHandlerGenerator.class.getPackage());
        superName = chooseSuperName();
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_7, 0, className, null, superName, null);

        GeneratorAdapter gen = new GeneratorAdapter(0, getMethod("void <init>()"), null, null, cw);
        gen.loadThis();
        gen.invokeConstructor(Type.getObjectType(superName), getMethod("void <init>()"));
        gen.returnValue();
        gen.endMethod();
    }

    private String chooseSuperName() {
        Class<?> superHClass = factory.getHandlerClass(clazz.getSuperclass());
        return Type.getInternalName(superHClass == null ? ToNbtHandler.class : superHClass);
    }

    private boolean needCallSuper() {
        return !superName.equals(Type.getInternalName(ToNbtHandler.class));
    }

    private void genFields() {
        String nbtSerializerDesc = Type.getDescriptor(NBTSerializer.class);
        String methodHandleDesc = Type.getDescriptor(MethodHandle.class);
        for (Property<?, ?> property : properties) {
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, identFor(property, SERIALIZER), nbtSerializerDesc, null, null).visitEnd();
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, identFor(property, GETTER), methodHandleDesc, null, null).visitEnd();
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, identFor(property, SETTER), methodHandleDesc, null, null).visitEnd();
        }
    }

    private void genCLInit() {
        Type iterType = Type.getType(Iterator.class);
        Type objectArrType = Type.getType(Object[].class);
        Type objectType = Type.getType(Object.class);
        Type nbtSerType = Type.getType(NBTSerializer.class);
        Type generatorType = Type.getType(BytecodeEmittingHandlerGenerator.class);
        Type methodHandleType = Type.getType(MethodHandle.class);
        Type myType = Type.getObjectType(className);

        Method iterNext = getMethod("Object next()");

        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
        gen.visitCode();

        int iterLocal = gen.newLocal(iterType);
        gen.invokeStatic(generatorType, new Method("getStaticInfo", iterType, new Type[0]));
        gen.storeLocal(iterLocal);

        for (Property<?, ?> property : properties) {
            gen.loadLocal(iterLocal);
            gen.invokeInterface(iterType, iterNext);
            gen.checkCast(objectArrType);

            gen.dup();
            gen.push(0);
            gen.arrayLoad(objectType);
            gen.checkCast(nbtSerType);
            gen.putStatic(myType, identFor(property, SERIALIZER), nbtSerType);

            gen.dup();
            gen.push(1);
            gen.arrayLoad(objectType);
            gen.checkCast(methodHandleType);
            gen.putStatic(myType, identFor(property, GETTER), methodHandleType);

            gen.push(2);
            gen.arrayLoad(objectType);
            gen.checkCast(methodHandleType);
            gen.putStatic(myType, identFor(property, SETTER), methodHandleType);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private void genWrite() {
        Method method = getMethod("void write(Object, net.minecraft.nbt.NBTTagCompound)");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.visitCode();

        if (needCallSuper()) {
            gen.loadThis();
            gen.loadArg(0);
            gen.loadArg(1);
            gen.invokeConstructor(Type.getObjectType(superName), method);
        }

        Type myType = Type.getObjectType(className);
        Type serializerType = Type.getType(NBTSerializer.class);
        Type nbtBaseType = Type.getType(NBTBase.class);
        Type stringType = Type.getType(String.class);
        Type nbtCompType = Type.getType(NBTTagCompound.class);

        Method serializerWrite = getMethod("net.minecraft.nbt.NBTBase write(Object)");
        Method nbtCompoundSet = new Method(/* MCPNames.method(MCPNames.M_SET_TAG)*/ "setTag", Type.VOID_TYPE, new Type[] { stringType, nbtBaseType });

        for (Property<?, ?> property : properties) {
            gen.loadArg(1);
            gen.push(property.getName());

            gen.getStatic(myType, identFor(property, SERIALIZER), serializerType);
            getValue(gen, property);
            ASMUtils.convertTypes(gen, property.getRawType(), Object.class);

            gen.invokeInterface(serializerType, serializerWrite);

            gen.invokeVirtual(nbtCompType, nbtCompoundSet);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private void genRead() {
        Method method = getMethod("void read(Object, net.minecraft.nbt.NBTTagCompound)");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.visitCode();

        if (needCallSuper()) {
            gen.loadThis();
            gen.loadArg(0);
            gen.loadArg(1);
            gen.invokeConstructor(Type.getObjectType(superName), method);
        }

        Type myType = Type.getObjectType(className);
        Type serializerType = Type.getType(NBTSerializer.class);
        Type nbtCompType = Type.getType(NBTTagCompound.class);
        Type nbtBaseType = Type.getType(NBTBase.class);
        Type stringType = Type.getType(String.class);

        Method nbtCompGetTag = new Method("getTag", nbtBaseType, new Type[] { stringType });
        Method serializerRead = getMethod("Object read(Object, net.minecraft.nbt.NBTBase)");

        for (Property<?, ?> property : properties) {
            prepareSetValue(gen, property);

            gen.getStatic(myType, identFor(property, SERIALIZER), serializerType);

            getValue(gen, property);
            ASMUtils.convertTypes(gen, property.getRawType(), Object.class);

            gen.loadArg(1);
            gen.push(property.getName());
            gen.invokeVirtual(nbtCompType, nbtCompGetTag);

            gen.invokeInterface(serializerType, serializerRead);
            ASMUtils.convertTypes(gen, Object.class, property.getRawType());

            doSetValue(gen, property);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private void getValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type myType = Type.getObjectType(className);
        Type methodHandleType = Type.getType(MethodHandle.class);
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);

        gen.getStatic(myType, identFor(property, GETTER), methodHandleType);
        gen.loadArg(0);
        gen.invokeVirtual(methodHandleType, new Method("invokeExact", propertyType, new Type[]{objectType}));
    }

    private void prepareSetValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type myType = Type.getObjectType(className);
        Type methodHandleType = Type.getType(MethodHandle.class);
        gen.getStatic(myType, identFor(property, SETTER), methodHandleType);
        gen.loadArg(0);
    }

    private void doSetValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);
        Type methodHandleType = Type.getType(MethodHandle.class);

        gen.invokeVirtual(methodHandleType, new Method("invokeExact", VOID_TYPE, new Type[] { objectType, propertyType }));
    }

    private Class<? extends ToNbtHandler> finish() {
        cw.visitEnd();
        Class<? extends ToNbtHandler> result;
        synchronized (BytecodeEmittingHandlerGenerator.class) {
            staticProperties = properties;
            //noinspection unchecked
            result = (Class<? extends ToNbtHandler>) SCReflection.defineDynamicClass(cw.toByteArray());
            staticProperties = null;
        }

        return result;
    }

    private static List<Property<?, ?>> staticProperties;

    @SuppressWarnings("unused") // called by the dynamic classes
    static Iterator<Object[]> getStaticInfo() {
        return FluentIterable.from(staticProperties)
                .transform(new Function<Property<?, ?>, Object[]>() {
                    @Override
                    public Object[] apply(Property<?, ?> property) {
                        Class<?> rawType = property.getRawType();
                        return new Object[]{
                                ToNbtFactories.serializerFor(property),
                                property.getGetter().asType(methodType(rawType, Object.class)),
                                property.getSetter().asType(methodType(void.class, Object.class, rawType))
                        };
                    }
                })
                .iterator();
    }

    private String identFor(Property<?, ?> property, String type) {
        Member member = property.getMember();
        return "_sc$"
                + (member instanceof java.lang.reflect.Method ? "m$" : "")
                + member.getName()
                + '$' + type;
    }

}
