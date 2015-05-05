package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.serialize.Property;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.IExtendedEntityProperties;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
public final class BytecodeEmittingCompanionGenerator {

    private static final String SYNCER = "syn";
    private static final String COMPANION = "com";
    private static final String GETTER = "get";
    private static final String SETTER = "set";

    private final DefaultCompanionFactory factory;
    private final Class<?> clazz;

    private String className;
    private String superName;
    private Class<?> superClass;
    private ClassWriter cw;
    private final Map<Property<?, ?>, Syncer<?, ?, ?>> properties;
    private int firstID;

    BytecodeEmittingCompanionGenerator(DefaultCompanionFactory factory, Class<?> clazz, Map<Property<?, ?>, Syncer<?, ?, ?>> properties) {
        this.factory = factory;
        this.clazz = clazz;
        this.properties = properties;
    }

    Class<?> generateCompanion() {
        beginClass();

        makeFields();
        makeCLInit();
        makeReadID();
        makeRead();

        makeWriteID();
        makeCheck();

        return finish();
    }

    private void beginClass() {
        className = SCReflection.nextDynamicClassName(BytecodeEmittingCompanionGenerator.class.getPackage());

        superClass = findAppropriateSuperClass();
        superName = Type.getInternalName(superClass);
        firstID = factory.getNextFreeIDFor(clazz);

        cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_PUBLIC, className, null, superName, null);

        Method cstr = getMethod("void <init>()");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, cstr, null, null, cw);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getObjectType(superName), cstr);
        gen.returnValue();
        gen.endMethod();
    }

    private Class<?> findAppropriateSuperClass() {
        Class<?> superClassCompanion = factory.getCompanionClass(clazz.getSuperclass());
        if (superClassCompanion == null) {
            return isIEEP() ? IEEPSyncCompanion.class : SyncCompanion.class;
        } else {
            return superClassCompanion;
        }
    }

    private boolean isIEEP() {
        return IExtendedEntityProperties.class.isAssignableFrom(clazz);
    }

    private void makeFields() {
        for (Map.Entry<Property<?, ?>, Syncer<?, ?, ?>> entry : properties.entrySet()) {
            Property<?, ?> property = entry.getKey();
            Syncer<?, ?, ?> syncer = entry.getValue();

            String descSyncer = Type.getDescriptor(Syncer.class);
            String descMH = Type.getDescriptor(MethodHandle.class);

            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getPropertyID(property, SYNCER), descSyncer, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getPropertyID(property, GETTER), descMH, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getPropertyID(property, SETTER), descMH, null, null);

            Class<?> companionType = syncer.getCompanionType();
            if (companionType != null) {
                cw.visitField(ACC_PRIVATE, getPropertyID(property, COMPANION), Type.getDescriptor(companionType), null, null);
            }
        }
    }

    private void makeRead() {
        Method method = getMethod("int read(Object, de.take_weiland.mods.commons.net.MCDataInput)");
        final GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.visitCode();

        final Type myType = Type.getObjectType(className);
        final Type mcDataInType = Type.getType(MCDataInput.class);
        final Type syncerType = Type.getType(Syncer.class);
        final Type objectType = Type.getType(Object.class);
        final Type valueHolderType = Type.getType(clazz);

        final int objectArg = 0;
        final int inStreamArg = 1;
        final int fieldID = gen.newLocal(Type.INT_TYPE);

        if (needCallSuper()) {
            gen.loadThis();
            gen.loadArg(0);
            gen.loadArg(1);
            gen.invokeConstructor(Type.getObjectType(superName), method);
            gen.storeLocal(fieldID);
        } else {
            gen.loadThis();
            gen.loadArg(inStreamArg);
            gen.invokeVirtual(myType, readIDMethod());
            gen.storeLocal(fieldID);
        }

        Label start = gen.mark();

        int keyCount = properties.size();
        int[] keys = new int[keyCount];
        for (int i = 0; i < keyCount; i++) {
            keys[i] = i + firstID;
        }

        gen.loadLocal(fieldID);
        gen.tableSwitch(keys, new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                Map.Entry<Property<?, ?>, Syncer<?, ?, ?>> entry = Iterables.get(properties.entrySet(), key - firstID);
                Property<?, ?> property = entry.getKey();
                Syncer<?, ?, ?> syncer = entry.getValue();

                boolean hasCompanion = syncer.getCompanionType() != null;
                Type companionType = hasCompanion ? Type.getType(syncer.getCompanionType()) : null;

                prepareSetValue(gen, property);

                gen.getStatic(myType, getPropertyID(property, SYNCER), syncerType);
                loadValue(gen, property);

                ASMUtils.convertTypes(gen, property.getRawType(), Object.class);

                if (hasCompanion) {
                    gen.loadThis();
                    gen.getField(myType, getPropertyID(property, COMPANION), companionType);
                    ASMUtils.convertTypes(gen, syncer.getCompanionType(), Object.class);
                } else {
                    gen.push((String) null); // type doesn't matter
                }
                gen.loadArg(inStreamArg);

                gen.invokeInterface(syncerType, new Method("read", objectType, new Type[]{objectType, objectType, mcDataInType}));

                ASMUtils.convertTypes(gen, Object.class, property.getRawType());
                doSetValue(gen, property);

                gen.goTo(end);
            }

            @Override
            public void generateDefault() {
                gen.loadLocal(fieldID); // unknown ID, either return control back to super caller or end of stream (-1)
                gen.returnValue();
            }
        });

        gen.loadThis();
        gen.loadArg(inStreamArg);
        gen.invokeVirtual(myType, readIDMethod());
        gen.storeLocal(fieldID);
        gen.goTo(start);

        gen.endMethod();
    }

    private void loadValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type myType = Type.getObjectType(className);
        Type mhType = Type.getType(MethodHandle.class);
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);

        gen.getStatic(myType, getPropertyID(property, GETTER), mhType);
        gen.loadArg(0);
        gen.invokeVirtual(mhType, new Method("invokeExact", propertyType, new Type[]{objectType}));
    }

    private void prepareSetValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type myType = Type.getObjectType(className);
        Type mhType = Type.getType(MethodHandle.class);

        gen.getStatic(myType, getPropertyID(property, SETTER), mhType);
        gen.loadArg(0);
    }

    private void doSetValue(GeneratorAdapter gen, Property<?, ?> property) {
        Type mhType = Type.getType(MethodHandle.class);
        Type propertyType = Type.getType(property.getRawType());
        Type objectType = Type.getType(Object.class);

        gen.invokeVirtual(mhType, new Method("invokeExact", Type.VOID_TYPE, new Type[] { objectType, propertyType }));
    }

    private static Method readIDMethod() {
        return getMethod("int readID(de.take_weiland.mods.commons.net.MCDataInput)");
    }

    private void makeReadID() {
        Method method = readIDMethod();
        GeneratorAdapter gen = new GeneratorAdapter(0, method, null, null, cw);
        gen.visitCode();

        Method readMethod = new Method("read" + idSize(true), INT_TYPE, new Type[0]);
        gen.loadArg(0);
        gen.invokeInterface(Type.getType(MCDataInput.class), readMethod);
        gen.returnValue();
        gen.endMethod();
    }

    private String idSize(boolean read) {
        int maxId = firstID + properties.size() - 1;
        if (maxId <= UnsignedBytes.toInt(UnsignedBytes.MAX_VALUE)) {
            return read ? "UnsignedByte" : "Byte";
        } else if (maxId <= UnsignedShorts.MAX_VALUE) {
            return read ? "UnsignedShort" : "Short";
        } else {
            return  "Int";
        }
    }

    private static Method writeIDMethod() {
        return getMethod("void writeID(de.take_weiland.mods.commons.net.MCDataOutput, int)");
    }

    private void makeWriteID() {
        Method method = writeIDMethod();
        GeneratorAdapter gen = new GeneratorAdapter(0, method, null, null, cw);
        gen.visitCode();

        Method writeMethod = new Method("writeTo" + idSize(false), VOID_TYPE, new Type[] { INT_TYPE });
        gen.loadArg(0);
        gen.loadArg(1);
        gen.invokeInterface(Type.getType(MCDataOutput.class), writeMethod);
        gen.returnValue();
        gen.endMethod();
    }

    private void makeCheck() {
        Method method = getMethod("de.take_weiland.mods.commons.net.MCDataOutput check(Object, boolean)");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
        gen.visitCode();

        Type mcDataOutType = Type.getType(MCDataOutput.class);
        Type syncHelpersType = Type.getType(SyncHelpers.class);
        Type myType = Type.getObjectType(className);
        Type valueHolderType = Type.getType(clazz);
        Type entityType = Type.getType(Entity.class);
        Type tileEntityType = Type.getType(TileEntity.class);
        Type containerType = Type.getType(Container.class);
        Type ieepCompType = Type.getType(IEEPSyncCompanion.class);
        Type syncerType = Type.getType(Syncer.class);

        int objectArg = 0;
        int superCallArg = 1;
        int outStreamID = gen.newLocal(mcDataOutType);

        if (needCallSuper()) {
            gen.loadThis();
            gen.loadArg(0);
            gen.push(true); // isSuperCall
            gen.invokeConstructor(getObjectType(superName), method);
        } else {
            gen.push((String) null); // don't care about the type
        }
        gen.storeLocal(outStreamID);

        Label next = null;

        SyncType syncType = SyncHelpers.getSyncType(clazz);

        int index = 0;
        for (Map.Entry<Property<?, ?>, Syncer<?, ?, ?>> entry : properties.entrySet()) {
            Property<?, ?> property = entry.getKey();
            Syncer<?, ?, ?> syncer = entry.getValue();

            if (next != null) {
                gen.mark(next);
            }
            next = new Label();

            boolean hasCompanion = syncer.getCompanionType() != null;
            Type companionType = hasCompanion ? Type.getType(syncer.getCompanionType()) : null;

            gen.getStatic(myType, getPropertyID(property, SYNCER), syncerType);
            loadValue(gen, property);
            ASMUtils.convertTypes(gen, property.getRawType(), Object.class);

            if (hasCompanion) {
                gen.loadThis();
                gen.getField(myType, getPropertyID(property, COMPANION), companionType);
                ASMUtils.convertTypes(gen, syncer.getCompanionType(), Object.class);
            } else {
                gen.push((String) null); // type doesnt matter
            }

            gen.invokeInterface(syncerType, getMethod("boolean equal(Object, Object)"));
            gen.ifZCmp(NE, next);

            Label nonNull = new Label();
            gen.loadLocal(outStreamID);
            gen.ifNonNull(nonNull);

            switch (syncType) {
                case ENTITY:
                    gen.loadArg(0);
                    gen.checkCast(entityType);
                    gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[] { entityType }));
                    break;
                case TILE_ENTITY:
                    gen.loadArg(0);
                    gen.checkCast(tileEntityType);
                    gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[] { tileEntityType }));
                    break;
                case CONTAINER:
                    gen.loadArg(0);
                    gen.checkCast(containerType);
                    gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[] { containerType }));
                    break;
                case ENTITY_PROPS:
                    gen.loadThis();
                    gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[] { ieepCompType }));
                    break;
            }
            gen.storeLocal(outStreamID);

            gen.mark(nonNull);

            gen.loadThis();
            gen.loadLocal(outStreamID);
            gen.push(firstID + index);
            gen.invokeVirtual(myType, writeIDMethod());

            if (hasCompanion) {
                gen.loadThis(); // for companion set
            }
            gen.getStatic(myType, getPropertyID(property, SYNCER), syncerType);
            loadValue(gen, property);
            ASMUtils.convertTypes(gen, property.getRawType(), Object.class);

            if (hasCompanion) {
                gen.loadThis();
                gen.getField(myType, getPropertyID(property, COMPANION), companionType);
                ASMUtils.convertTypes(gen, syncer.getCompanionType(), Object.class);
            } else {
                gen.push((String) null);
            }
            gen.loadLocal(outStreamID);
            gen.invokeInterface(syncerType, getMethod("Object writeAndUpdate(Object, Object, de.take_weiland.mods.commons.net.MCDataOutput)"));
            if (hasCompanion) {
                ASMUtils.convertTypes(gen, Object.class, syncer.getCompanionType());
                gen.putField(myType, getPropertyID(property, COMPANION), companionType);
            } else {
                gen.pop();
            }
        }

        if (next != null) {
            gen.mark(next);
        }
        Label end = new Label();
        gen.loadArg(1);
        gen.ifZCmp(NE, end);
        gen.loadLocal(outStreamID);
        gen.ifNull(end);

        gen.loadThis();
        gen.loadLocal(outStreamID);
        gen.push(0);
        gen.invokeVirtual(myType, writeIDMethod());

        switch (syncType) {
            case ENTITY:
                gen.loadArg(0);
                gen.checkCast(entityType);
                gen.loadLocal(outStreamID);
                gen.invokeStatic(syncHelpersType, new Method("sendStream", VOID_TYPE, new Type[] { entityType, mcDataOutType }));
                break;
            case TILE_ENTITY:
                gen.loadArg(0);
                gen.checkCast(tileEntityType);
                gen.loadLocal(outStreamID);
                gen.invokeStatic(syncHelpersType, new Method("sendStream", VOID_TYPE, new Type[] { tileEntityType, mcDataOutType }));
                break;
            case CONTAINER:
                gen.loadArg(0);
                gen.checkCast(containerType);
                gen.loadLocal(outStreamID);
                gen.invokeStatic(syncHelpersType, new Method("sendStream", VOID_TYPE, new Type[] { containerType, mcDataOutType }));
                break;
            case ENTITY_PROPS:
                gen.loadThis();
                gen.getField(ieepCompType, "_sc$entity", entityType);
                gen.loadLocal(outStreamID);
                gen.invokeStatic(syncHelpersType, new Method("sendStream", VOID_TYPE, new Type[] { entityType, mcDataOutType }));
                break;
        }

        gen.mark(end);
        gen.loadLocal(outStreamID);
        gen.returnValue();
        gen.endMethod();
    }

    private boolean needCallSuper() {
        return superClass != SyncCompanion.class && superClass != IEEPSyncCompanion.class;
    }

    private void makeCLInit() {
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
        gen.visitCode();

        Type myType = getObjectType(className);
        Type iteratorType = Type.getType(Iterator.class);
        Type objectArrType = Type.getType(Object[].class);
        Type objectType = Type.getType(Object.class);
        Type methodHandleType = Type.getType(MethodHandle.class);
        Type syncerType = Type.getType(Syncer.class);

        int iterator = gen.newLocal(iteratorType);
        gen.invokeStatic(Type.getType(BytecodeEmittingCompanionGenerator.class), getMethod("java.util.Iterator getStaticData()"));
        gen.storeLocal(iterator);

        for (Property<?, ?> property : properties.keySet()) {
            gen.loadLocal(iterator);
            gen.invokeInterface(iteratorType, getMethod("Object next()"));
            gen.checkCast(objectArrType);

            gen.dup();
            gen.push(0);
            gen.arrayLoad(objectType);
            gen.checkCast(syncerType);
            gen.putStatic(myType, getPropertyID(property, SYNCER), syncerType);

            gen.dup();
            gen.push(1);
            gen.arrayLoad(objectType);
            gen.checkCast(methodHandleType);
            gen.putStatic(myType, getPropertyID(property, GETTER), methodHandleType);

            gen.push(2);
            gen.arrayLoad(objectType);
            gen.checkCast(methodHandleType);
            gen.putStatic(myType, getPropertyID(property, SETTER), methodHandleType);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private static String getPropertyID(Property<?, ?> property, String role) {
        return getPropertyID(property) + "$" + role;
    }

    private static String getPropertyID(Property<?, ?> property) {
        return property.getName() + (property.getMember() instanceof Field ? "$f" : "$m");
    }

    private static Map<Property<?, ?>, Syncer<?, ?, ?>> staticProperties;

    private Class<?> finish() {
        Class<?> cls;

        // gross hack, please close your eyes
        synchronized (BytecodeEmittingCompanionGenerator.class) {
            // <clinit> of generated class calls getMHMap

            staticProperties = properties;
            try {
                cw.visitEnd();
                cls = SCReflection.defineDynamicClass(cw.toByteArray(), BytecodeEmittingCompanionGenerator.class);
            } finally{
                staticProperties = null;
            }
        }

        return cls;
    }

    // called from <clinit> in generated classes, see #finish()
    @SuppressWarnings("unused")
    static Iterator<Object[]> getStaticData() throws NoSuchFieldException, IllegalAccessException {
        return FluentIterable.from(staticProperties.entrySet())
                .transform(entry -> {
                    Property<?, ?> property = entry.getKey();
                    Syncer<?, ?, ?> syncer = entry.getValue();

                    return new Object[] {
                            syncer,
                            property.getGetter().asType(methodType(property.getRawType(), Object.class)),
                            property.getSetter().asType(methodType(void.class, Object.class, property.getRawType()))
                    };
                })
                .iterator();
    }

}
