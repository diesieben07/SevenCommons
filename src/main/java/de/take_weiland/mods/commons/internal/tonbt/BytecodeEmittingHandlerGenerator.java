package de.take_weiland.mods.commons.internal.tonbt;

import com.google.common.collect.FluentIterable;
import de.take_weiland.mods.commons.internal.prop.AbstractProperty;
import de.take_weiland.mods.commons.nbt.NBTData;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.reflect.SCReflection;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
public final class BytecodeEmittingHandlerGenerator {

    private static final String SERIALIZER = "ser";
    private static final String GETTER = "get";
    private static final String PROP_ACC = "prp";
    private static final String SETTER = "set";

    private final DefaultHandlerFactory factory;
    private final Class<?> clazz;
    private ClassWriter cw;
    private String className;
    private String superName;
    private Map<Property<?>, NBTSerializer<?>> properties;

    BytecodeEmittingHandlerGenerator(DefaultHandlerFactory factory, Class<?> clazz) {
        this.factory = factory;
        this.clazz = clazz;
    }

    Class<? extends ToNbtHandler> generateHandler() {
        properties = AbstractProperty.allPropertiesLazy(clazz, ToNbt.class)
                .toMap(ToNbtFactories::serializerFor);
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
        String nbtSerializerValDesc = Type.getDescriptor(NBTSerializer.ForValue.class);
        String nbtSerializerContDesc = Type.getDescriptor(NBTSerializer.ForContainer.class);
        String propertyAccessDesc = Type.getDescriptor(PropertyAccess.class);

        for (Map.Entry<Property<?>, NBTSerializer<?>> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            NBTSerializer<?> ser = entry.getValue();
            String serFieldDesc = ser instanceof NBTSerializer.ForValue ? nbtSerializerValDesc
                    : ser instanceof NBTSerializer.ForContainer ? nbtSerializerContDesc
                    : nbtSerializerDesc;
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, identFor(property, SERIALIZER), serFieldDesc, null, null).visitEnd();
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, identFor(property, PROP_ACC), propertyAccessDesc, null, null).visitEnd();
        }
    }

    private void genCLInit() {
        Type iterType = Type.getType(Iterator.class);
        Type objectArrType = Type.getType(Object[].class);
        Type objectType = Type.getType(Object.class);
        Type generatorType = Type.getType(BytecodeEmittingHandlerGenerator.class);
        Type propertyAccessType = Type.getType(PropertyAccess.class);
        Type myType = Type.getObjectType(className);

        Method iterNext = getMethod("Object next()");

        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
        gen.visitCode();

        int iterLocal = gen.newLocal(iterType);
        gen.invokeStatic(generatorType, new Method("getStaticInfo", iterType, new Type[0]));
        gen.storeLocal(iterLocal);

        for (Map.Entry<Property<?>, NBTSerializer<?>> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            NBTSerializer<?> serializer = entry.getValue();

            gen.loadLocal(iterLocal);
            gen.invokeInterface(iterType, iterNext);
            gen.checkCast(objectArrType);

            Type serializerType = Type.getType(getSerializerType(serializer));

            gen.dup();
            gen.push(0);
            gen.arrayLoad(objectType);
            gen.checkCast(serializerType);
            gen.putStatic(myType, identFor(property, SERIALIZER), serializerType);

            gen.push(1);
            gen.arrayLoad(objectType);
            gen.checkCast(propertyAccessType);
            gen.putStatic(myType, identFor(property, PROP_ACC), propertyAccessType);
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
        Type objectType = Type.getType(Object.class);
        Type propertyAccessType = Type.getType(PropertyAccess.class);

        for (Map.Entry<Property<?>, NBTSerializer<?>> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            NBTSerializer<?> serializer = entry.getValue();

            gen.loadArg(1);
            gen.push(property.getName());

            gen.getStatic(myType, identFor(property, SERIALIZER), Type.getType(getSerializerType(serializer)));
            gen.loadArg(0);
            gen.getStatic(myType, identFor(property, PROP_ACC), propertyAccessType);
            gen.invokeInterface(serializerType, new Method("write", nbtBaseType, new Type[]{objectType, propertyAccessType}));

            gen.invokeVirtual(nbtCompType, new Method(/* MCPNames.method(MCPNames.M_SET_TAG)*/ "setTag", Type.VOID_TYPE, new Type[]{stringType, nbtBaseType}));
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
        Type objectType = Type.getType(Object.class);
        Type propertyAccessType = Type.getType(PropertyAccess.class);

        Method nbtCompGetTag = new Method("getTag", nbtBaseType, new Type[]{stringType});

        final int nbtTagSlot = gen.newLocal(nbtBaseType);

        for (Map.Entry<Property<?>, NBTSerializer<?>> entry : properties.entrySet()) {
            Property<?> property = entry.getKey();
            NBTSerializer<?> serializer = entry.getValue();

            gen.loadArg(1);
            gen.push(property.getName());
            gen.invokeVirtual(nbtCompType, nbtCompGetTag);
            gen.storeLocal(nbtTagSlot);

            Label isNull = new Label();
            gen.loadLocal(nbtTagSlot);
            gen.ifNull(isNull);

            gen.getStatic(myType, identFor(property, SERIALIZER), Type.getType(getSerializerType(serializer)));
            gen.loadLocal(nbtTagSlot);
            gen.loadArg(0);
            gen.getStatic(myType, identFor(property, PROP_ACC), propertyAccessType);
            gen.invokeInterface(serializerType, new Method("read", VOID_TYPE, new Type[]{nbtBaseType, objectType, propertyAccessType}));

            gen.mark(isNull);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private Class<?> getSerializerType(NBTSerializer<?> serializer) {
        if (serializer instanceof NBTSerializer.ForValue) {
            return NBTSerializer.ForValue.class;
        } else if (serializer instanceof NBTSerializer.ForContainer) {
            return NBTSerializer.ForContainer.class;
        } else {
            return NBTSerializer.class;
        }
    }

    private void pushDefaultValue(GeneratorAdapter gen, Class<?> type) {
        if (type.isPrimitive()) {
            switch (Type.getType(type).getSort()) {
                case Type.FLOAT:
                    gen.push(0f);
                    break;
                case Type.DOUBLE:
                    gen.push(0d);
                    break;
                default:
                    gen.push(0);
                    break;
            }
        } else {
            gen.push((String) null);
        }
    }

    private void getValue(GeneratorAdapter gen, Property<?> property) {
        Type myType = Type.getObjectType(className);
        Type methodHandleType = Type.getType(MethodHandle.class);
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);

        gen.getStatic(myType, identFor(property, GETTER), methodHandleType);
        gen.loadArg(0);
        gen.invokeVirtual(methodHandleType, new Method("invokeExact", propertyType, new Type[]{objectType}));
    }

    private void prepareSetValue(GeneratorAdapter gen, Property<?> property) {
        Type myType = Type.getObjectType(className);
        Type methodHandleType = Type.getType(MethodHandle.class);
        gen.getStatic(myType, identFor(property, SETTER), methodHandleType);
        gen.loadArg(0);
    }

    private void doSetValue(GeneratorAdapter gen, Property<?> property) {
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);
        Type methodHandleType = Type.getType(MethodHandle.class);

        gen.invokeVirtual(methodHandleType, new Method("invokeExact", VOID_TYPE, new Type[]{objectType, propertyType}));
    }

    private Class<? extends ToNbtHandler> finish() {
        cw.visitEnd();
        Class<? extends ToNbtHandler> result;
        synchronized (BytecodeEmittingHandlerGenerator.class) {
            staticProperties = properties;
            //noinspection unchecked
            result = (Class<? extends ToNbtHandler>) SCReflection.defineClass(cw.toByteArray());
            staticProperties = null;
        }

        return result;
    }

    private static Map<Property<?>, NBTSerializer<?>> staticProperties;

    @SuppressWarnings("unused") // called by the dynamic classes
    static Iterator<Object[]> getStaticInfo() {
        return FluentIterable.from(staticProperties.entrySet())
                .transform(entry -> {
                    Property<?> property = entry.getKey();
                    NBTSerializer<?> serializer = entry.getValue();

                    Class<?> rawType = property.getRawType();
                    return new Object[]{
                            serializer,
                            property.optimize()
                    };
                })
                .iterator();
    }

    private String identFor(Property<?> property, String type) {
        Member member = property.getMember();
        return "_sc$"
                + (member instanceof java.lang.reflect.Method ? "m$" : "")
                + member.getName()
                + '$' + type;
    }

    public static <T> NBTBase write(NBTSerializer<T> serializer, T val) {
        return val == null ? NBTData.serializedNull() : serializer.write(val);
    }

}
