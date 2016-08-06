//package de.take_weiland.mods.commons.internal.sync_olds;
//
//import com.google.common.collect.Iterables;
//import de.take_weiland.mods.commons.internal.prop.AbstractProperty;
//import de.take_weiland.mods.commons.reflect.Property;
//import de.take_weiland.mods.commons.reflect.PropertyAccess;
//import de.take_weiland.mods.commons.reflect.SCReflection;
//import de.take_weiland.mods.commons.sync.Sync;
//import de.take_weiland.mods.commons.sync.TypeSyncer;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.inventory.Container;
//import net.minecraft.tileentity.TileEntity;
//import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.Label;
//import org.objectweb.asm.Type;
//import org.objectweb.asm.commons.GeneratorAdapter;
//import org.objectweb.asm.commons.Method;
//import org.objectweb.asm.commons.TableSwitchGenerator;
//
//import java.lang.reflect.Field;
//import java.util.Iterator;
//import java.util.Map;
//
//import static org.objectweb.asm.Opcodes.*;
//import static org.objectweb.asm.Type.VOID_TYPE;
//import static org.objectweb.asm.Type.getObjectType;
//import static org.objectweb.asm.commons.GeneratorAdapter.*;
//import static org.objectweb.asm.commons.Method.getMethod;
//
///**
// * @author diesieben07
// */
//public final class BytecodeEmittingCompanionGenerator_OLD {
//
//    public static final int PRV_STC_FNL = ACC_PRIVATE | ACC_STATIC | ACC_FINAL;
//    private static final String SYNCER = "syn";
//    private static final String COMPANION = "com";
//    private static final String PROP_ACC = "prp";
//    private static final String COMP_ACC = "cop";
//
//    private final DefaultCompanionFactory factory;
//    private final Class<?> clazz;
//
//    private String className;
//    private String superName;
//    private Class<?> superClass;
//    private ClassWriter cw;
//    private final Map<Property<?>, TypeSyncer<?, ?, ?>> properties;
//    private int firstID;
//
//    BytecodeEmittingCompanionGenerator_OLD(DefaultCompanionFactory factory, Class<?> clazz, Map<Property<?>, TypeSyncer<?, ?, ?>> properties) {
//        this.factory = factory;
//        this.clazz = clazz;
//        this.properties = properties;
//    }
//
//    Class<?> generateCompanion() {
//        beginClass();
//
//        makeFields();
//        makeCLInit();
//        makeApplyChanges();
//
////        makeForceUpdate();
////        makeCheck();
//        genCheck(true);
//        genCheck(false);
//
//        return finish();
//    }
//
//    private void beginClass() {
//        className = SCReflection.nextDynamicClassName(BytecodeEmittingCompanionGenerator_OLD.class.getPackage());
//
//        superClass = findAppropriateSuperClass();
//        superName = Type.getInternalName(superClass);
//        firstID = factory.getNextFreeIDFor(clazz);
//
//        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//        cw.visit(V1_8, ACC_PUBLIC, className, null, superName, null);
//
//        Method cstr = getMethod("void <init>()");
//        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, cstr, null, null, cw);
//        gen.visitCode();
//        gen.loadThis();
//        gen.invokeConstructor(Type.getObjectType(superName), cstr);
//        gen.returnValue();
//        gen.endMethod();
//    }
//
//    private Class<?> findAppropriateSuperClass() {
//        Class<?> superClassCompanion = factory.getCompanionClass(clazz.getSuperclass());
//        if (superClassCompanion == null) {
//            return isIEEP() ? IEEPSyncCompanion.class : SyncCompanion.class;
//        } else {
//            return superClassCompanion;
//        }
//    }
//
//    private boolean isIEEP() {
//        return IExtendedEntityProperties.class.isAssignableFrom(clazz);
//    }
//
//    private void makeFields() {
//        for (Map.Entry<Property<?>, TypeSyncer<?, ?, ?>> entry : properties.entrySet()) {
//            Property<?> property = entry.getKey();
//            TypeSyncer<?, ?, ?> syncer = entry.getValue();
//
//            String descSyncer = Type.getDescriptor(TypeSyncer.class);
//            String descPropAcc = Type.getDescriptor(PropertyAccess.class);
//
//            cw.visitField(PRV_STC_FNL, getPropertyID(property, SYNCER), descSyncer, null, null);
//            cw.visitField(PRV_STC_FNL, getPropertyID(property, PROP_ACC), descPropAcc, null, null);
//
//            Class<?> companionType = syncer.companionType();
//            if (companionType != null) {
//                cw.visitField(PRV_STC_FNL, getPropertyID(property, COMP_ACC), descPropAcc, null, null);
//
//                cw.visitField(ACC_PUBLIC, getPropertyID(property, COMPANION), Type.getDescriptor(companionType), null, null);
//            }
//        }
//    }
//
//    private void makeApplyChanges() {
//        Method method = getMethod("int applyChanges(Object, de.take_weiland.mods.commons.internal.sync.SyncCompanion$ChangeIterator)");
//        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
//        gen.visitCode();
//
//        final Type myType = Type.getObjectType(className);
//        final Type superType = Type.getType(superClass);
//        final Type changeItType = Type.getType(SyncCompanion.ChangeIterator.class);
//        final Type syncerType = Type.getType(TypeSyncer.class);
//        final Type objectType = Type.getType(Object.class);
//        final Type propertyAccessType = Type.getType(PropertyAccess.class);
//
//        final int inStreamArg = 1;
//        final int fieldID = gen.newLocal(Type.INT_TYPE);
//
//        if (needCallSuper()) {
//            gen.loadThis();
//            gen.loadArg(0);
//            gen.loadArg(1);
//            gen.invokeConstructor(superType, method);
//            gen.storeLocal(fieldID);
//        } else {
//            gen.loadArg(1);
//            gen.invokeInterface(changeItType, getMethod("int fieldId()"));
//            gen.storeLocal(fieldID);
//        }
//
//        Label start = gen.mark();
//
//        int keyCount = properties.size();
//        int[] keys = new int[keyCount];
//        for (int i = 0; i < keyCount; i++) {
//            keys[i] = i + firstID;
//        }
//
//        gen.loadLocal(fieldID);
//        gen.tableSwitch(keys, new TableSwitchGenerator() {
//            @Override
//            public void generateCase(int key, Label end) {
//                Map.Entry<Property<?>, TypeSyncer<?, ?, ?>> entry = Iterables.get(properties.entrySet(), key - firstID);
//                Property<?> property = entry.getKey();
//                TypeSyncer<?, ?, ?> syncer = entry.getValue();
//
//                gen.loadArg(1);
//                gen.getStatic(myType, getPropertyID(property, SYNCER), syncerType);
//                gen.loadArg(0);
//                gen.getStatic(myType, getPropertyID(property, PROP_ACC), propertyAccessType);
//                gen.loadThis();
//                gen.getStatic(myType, getPropertyID(property, COMP_ACC), propertyAccessType);
//                gen.invokeInterface(changeItType, new Method("apply", VOID_TYPE,
//                        new Type[]{syncerType, objectType, propertyAccessType, objectType, propertyAccessType}));
//                gen.goTo(end);
//            }
//
//            @Override
//            public void generateDefault() {
//                gen.loadLocal(fieldID); // unknown ID, either return control back to super caller or end of stream
//                gen.returnValue();
//            }
//        });
//
//        gen.loadArg(1);
//        gen.invokeInterface(changeItType, getMethod("int fieldId()"));
//        gen.storeLocal(fieldID);
//        gen.goTo(start);
//
//        gen.endMethod();
//    }
//
//    private void genCheck(boolean inContainer) {
//        Class<?> syncEventClass = getSyncEventClass();
//        Type syncEventSubType = Type.getType(syncEventClass);
//
//        Type syncEventType = Type.getType(SyncEvent.class);
//        Type objectType = Type.getType(Object.class);
//        Type entityPlayerMPType = Type.getType(EntityPlayerMP.class);
//        Type myType = Type.getObjectType(className);
//        Type syncerType = Type.getType(TypeSyncer.class);
//        Type propertyAccessType = Type.getType(PropertyAccess.class);
//        Type changeType = Type.getType(TypeSyncer.Change.class);
//
//        String name = "check" + (inContainer ? "InContainer" : "");
//        String desc = Type.getMethodDescriptor(syncEventType, objectType, Type.INT_TYPE, entityPlayerMPType);
//
//        Method method = new Method(name, desc);
//        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);
//        gen.visitCode();
//
//        boolean hasProp = false;
//        for (Property<?> property : properties.keySet()) {
//            if (property.getAnnotation(Sync.class).inContainer() == inContainer) {
//                hasProp = true;
//                break;
//            }
//        }
//
//        final int p_syncObj = 0;
//        final int p_flags = 1;
//        final int p_player = 2;
//
//        if (!hasProp) {
//            if (needCallSuper()) {
//                gen.loadThis();
//                gen.loadArg(p_syncObj);
//                gen.loadArg(p_flags);
//                gen.push(SyncCompanion.SUPER_CALL);
//                gen.math(OR, Type.INT_TYPE);
//                gen.loadArg(p_player);
//                gen.invokeConstructor(Type.getType(superClass), method);
//            } else {
//                gen.push((String) null);
//            }
//            gen.returnValue();
//            gen.endMethod();
//            return;
//        }
//
//        final int v_syncEvent = gen.newLocal(syncEventType);
//        final int v_isForced = gen.newLocal(Type.BOOLEAN_TYPE);
//        final int v_change = gen.newLocal(changeType);
//
//        gen.loadArg(p_flags);
//        gen.push(SyncCompanion.FORCE_CHECK);
//        gen.math(AND, Type.INT_TYPE);
//        gen.storeLocal(v_isForced);
//
//        if (needCallSuper()) {
//            gen.loadThis();
//            gen.loadArg(p_syncObj);
//            gen.loadArg(p_flags);
//            gen.push(SyncCompanion.SUPER_CALL);
//            gen.math(OR, Type.INT_TYPE);
//            gen.loadArg(p_player);
//            gen.invokeConstructor(Type.getType(superClass), method);
//            gen.storeLocal(v_syncEvent);
//        } else {
//            Label doInit = new Label();
//            Label done = new Label();
//
//            gen.loadLocal(v_isForced);
//            gen.ifZCmp(NE, doInit);
//
//            gen.push((String) null);
//            gen.goTo(done);
//
//            gen.mark(doInit);
//            gen.newInstance(syncEventSubType);
//            gen.dup();
//            gen.loadArg(p_syncObj);
//            gen.invokeConstructor(syncEventSubType, new Method("<init>", Type.getMethodDescriptor(Type.VOID_TYPE, objectType)));
//
//            gen.mark(done);
//            gen.storeLocal(v_syncEvent);
//        }
//
//        int fieldIndex = -1;
//
//        for (Map.Entry<Property<?>, TypeSyncer<?, ?, ?>> entry : properties.entrySet()) {
//            Property<?> property = entry.getKey();
//            TypeSyncer<?, ?, ?> syncer = entry.getValue();
//
//            fieldIndex++;
//
//            if (property.getAnnotation(Sync.class).inContainer() != inContainer) {
//                continue;
//            }
//
//            gen.getStatic(myType, getPropertyID(property, SYNCER), syncerType);
//            gen.loadArg(p_syncObj);
//            gen.getStatic(myType, getPropertyID(property, PROP_ACC), propertyAccessType);
//            if (syncer.companionType() != null) {
//                gen.loadThis();
//                gen.getStatic(myType, getPropertyID(property, COMP_ACC), propertyAccessType);
//            } else {
//                gen.push((String) null);
//                gen.push((String) null);
//            }
//
//            Label invokeForced = new Label();
//            Label after = new Label();
//            gen.loadLocal(v_isForced);
//            gen.ifZCmp(NE, invokeForced);
//
//            gen.invokeInterface(syncerType, new Method("check", changeType, new Type[]{objectType, propertyAccessType, objectType, propertyAccessType}));
//            gen.goTo(after);
//            gen.mark(invokeForced);
//            gen.invokeInterface(syncerType, new Method("forceUpdate", changeType, new Type[]{objectType, propertyAccessType, objectType, propertyAccessType}));
//            gen.mark(after);
//            gen.storeLocal(v_change);
//
//            Label next = new Label();
//
//            gen.loadLocal(v_change);
//            gen.ifNull(next);
//
//            Label noInit = new Label();
//            gen.loadLocal(v_syncEvent);
//            gen.ifNonNull(noInit);
//
//            gen.newInstance(syncEventSubType);
//            gen.dup();
//            gen.loadArg(p_syncObj);
//            gen.invokeConstructor(syncEventSubType, new Method("<init>", Type.VOID_TYPE, new Type[]{objectType}));
//            gen.storeLocal(v_syncEvent);
//            gen.mark(noInit);
//
//            gen.loadLocal(v_syncEvent);
//            gen.push(firstID + fieldIndex);
//            gen.loadLocal(v_change);
//            gen.invokeVirtual(syncEventType, new Method("add", Type.VOID_TYPE, new Type[]{Type.INT_TYPE, changeType}));
//
//            gen.mark(next);
//        }
//
//        Label done = new Label();
//
//        gen.loadArg(p_flags);
//        gen.push(SyncCompanion.SUPER_CALL);
//        gen.math(AND, Type.INT_TYPE);
//        gen.ifZCmp(NE, done);
//
//        gen.loadLocal(v_syncEvent);
//        gen.ifNull(done);
//
//        gen.loadLocal(v_syncEvent);
//        gen.loadArg(p_syncObj);
//        gen.loadArg(p_player);
//        gen.invokeVirtual(syncEventType, new Method("send", Type.VOID_TYPE, new Type[]{objectType, entityPlayerMPType}));
//
//        gen.mark(done);
//        gen.loadLocal(v_syncEvent);
//        gen.returnValue();
//        gen.endMethod();
//    }
//
//    private Class<? extends SyncEvent> getSyncEventClass() {
//        if (TileEntity.class.isAssignableFrom(clazz)) {
//            return SyncEvent.ForTE.class;
//        } else if (Entity.class.isAssignableFrom(clazz)) {
//            return SyncEvent.ForEntity.class;
//        } else if (Container.class.isAssignableFrom(clazz)) {
//            return SyncEvent.ForContainer.class;
//        } else if (IExtendedEntityProperties.class.isAssignableFrom(clazz)) {
//            return SyncEvent.ForIEEP.class;
//        } else {
//            throw new IllegalStateException("Don't know how to sync in " + clazz);
//        }
//    }
//
//    private boolean needCallSuper() {
//        return superClass != SyncCompanion.class && superClass != IEEPSyncCompanion.class;
//    }
//
//    private void makeCLInit() {
//        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
//        gen.visitCode();
//
//        Type myType = getObjectType(className);
//        Type iteratorType = Type.getType(Iterator.class);
//        Type objectArrType = Type.getType(Object[].class);
//        Type objectType = Type.getType(Object.class);
//        Type syncerType = Type.getType(TypeSyncer.class);
//        Type propertyAccessType = Type.getType(PropertyAccess.class);
//
//        int iterator = gen.newLocal(iteratorType);
//
//        gen.push(myType);
//        gen.invokeStatic(Type.getType(BytecodeEmittingCompanionGenerator_OLD.class), getMethod("java.util.Iterator getStaticData(Class)"));
//        gen.storeLocal(iterator);
//
//        for (Map.Entry<Property<?>, TypeSyncer<?, ?, ?>> entry : properties.entrySet()) {
//            Property<?> property = entry.getKey();
//            TypeSyncer<?, ?, ?> syncer = entry.getValue();
//
//            gen.loadLocal(iterator);
//            gen.invokeInterface(iteratorType, getMethod("Object next()"));
//            gen.checkCast(objectArrType);
//
//            gen.dup();
//            gen.push(0);
//            gen.arrayLoad(objectType);
//            gen.checkCast(syncerType);
//            gen.putStatic(myType, getPropertyID(property, SYNCER), syncerType);
//
//            if (syncer.companionType() != null) {
//                gen.dup();
//            }
//            gen.push(1);
//            gen.arrayLoad(objectType);
//            gen.checkCast(propertyAccessType);
//            gen.putStatic(myType, getPropertyID(property, PROP_ACC), propertyAccessType);
//
//            if (syncer.companionType() != null) {
//                gen.push(2);
//                gen.arrayLoad(objectType);
//                gen.checkCast(propertyAccessType);
//                gen.putStatic(myType, getPropertyID(property, COMP_ACC), propertyAccessType);
//            }
//        }
//
//        gen.returnValue();
//        gen.endMethod();
//    }
//
//    private static String getPropertyID(Property<?> property, String role) {
//        return getPropertyID(property) + "$" + role;
//    }
//
//    private static String getPropertyID(Property<?> property) {
//        return property.getName() + (property.getMember() instanceof Field ? "$f" : "$m");
//    }
//
//    private static Map<Property<?>, TypeSyncer<?, ?, ?>> staticProperties;
//
//    private Class<?> finish() {
//        Class<?> cls;
//
//        // gross hack, please close your eyes
//        synchronized (BytecodeEmittingCompanionGenerator_OLD.class) {
//            // <clinit> of generated class calls getStaticData
//
//            staticProperties = properties;
//            try {
//                cw.visitEnd();
//                cls = SCReflection.defineClass(cw.toByteArray());
//            } finally {
//                staticProperties = null;
//            }
//        }
//
//        return cls;
//    }
//
//    // called from <clinit> in generated classes, see #finish()
//    @SuppressWarnings("unused")
//    static Iterator<Object[]> getStaticData(Class<?> generatedClass) throws NoSuchFieldException, IllegalAccessException {
//        return staticProperties.entrySet().stream()
//                .map(entry -> {
//                    Property<?> property = entry.getKey();
//                    TypeSyncer<?, ?, ?> syncer = entry.getValue();
//
//                    PropertyAccess<?> propertyAccess = property.optimize();
//                    PropertyAccess<?> compAccess;
//                    if (syncer.companionType() != null) {
//                        try {
//                            Field compField = generatedClass.getDeclaredField(getPropertyID(property, COMPANION));
//                            compAccess = AbstractProperty.newProperty(compField).optimize();
//                        } catch (ReflectiveOperationException e) {
//                            throw new IllegalStateException(e);
//                        }
//                    } else {
//                        compAccess = null; // not used anyways
//                    }
//
//                    return new Object[]{
//                            syncer,
//                            propertyAccess,
//                            compAccess
//                    };
//                })
//                .iterator();
//    }
//
//}
