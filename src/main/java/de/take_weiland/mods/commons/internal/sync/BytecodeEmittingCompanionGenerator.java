package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Maps;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.SCReflection;
import de.take_weiland.mods.commons.sync.SyncerFactory;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.TableSwitchGenerator;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.publicLookup;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
final class BytecodeEmittingCompanionGenerator {

    private static final String COMPANION = "companion";
    private static final String CHECKER = "checker";
    private static final int CHECKER_IN_ARR = 0;

    private static final String READER = "reader";
    private static final int READER_IN_ARR = 1;

    private static final String WRITER = "writer";
    private static final int WRITER_IN_ARR = 2;

    private final DefaultCompanionFactory factory;
    private final Class<?> clazz;

    private String className;
    private String superName;
    private Class<?> superClass;
    private ClassWriter cw;
    private List<CompanionFactory.SyncedMemberInfo> handles;
    private int firstID;

    BytecodeEmittingCompanionGenerator(DefaultCompanionFactory factory, Class<?> clazz) {
        this.factory = factory;
        this.clazz = clazz;
    }

    Class<?> generateCompanion() {
        handles = factory.getSyncedMemberInfo(clazz);
        if (handles.isEmpty()) {
            return null;
        }
        beginClass();

        makeMHFields();
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
        GeneratorAdapter gen = new GeneratorAdapter(0, cstr, null, null, cw);
        gen.loadThis();
        gen.invokeConstructor(Type.getObjectType(superName), cstr);
        gen.returnValue();
        gen.endMethod();
    }

    private Class<?> findAppropriateSuperClass() {
        Class<?> superClassCompanion = factory.getCompanionClass(clazz.getSuperclass());
        return superClassCompanion == null ? SyncCompanion.class : superClassCompanion;
    }

    private void makeMHFields() {
        for (CompanionFactory.SyncedMemberInfo info : handles) {
            String descMH = Type.getDescriptor(MethodHandle.class);
            String descCompanion = Type.getDescriptor(info.handle.getCompanionType());

            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, CHECKER), descMH, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, READER), descMH, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, WRITER), descMH, null, null);
            cw.visitField(ACC_PRIVATE, getMemberID(info.member, COMPANION), descCompanion, null, null);
        }
    }

    private void makeRead() {
        Method method = getMethod("int read(Object, de.take_weiland.mods.commons.net.MCDataInput)");
        final GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);

        final Type myType = Type.getObjectType(className);
        final Type mhType = Type.getType(MethodHandle.class);
        final Type mcDataInType = Type.getType(MCDataInput.class);
        final Type classToSyncType = Type.getType(clazz);

        final int objectArg = 0;
        final int inStreamArg = 1;
        final int fieldID = gen.newLocal(Type.INT_TYPE);

        if (!superName.equals(Type.getInternalName(SyncCompanion.class))) {
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

        final Method invokeExact = new Method("invokeExact", VOID_TYPE, new Type[] { classToSyncType, myType, mcDataInType });

        Label start = gen.mark();

        int keyCount = handles.size();
        int[] keys = new int[keyCount];
        for (int i = 0; i < keyCount; i++) {
            keys[i] = i + firstID;
        }

        gen.loadLocal(fieldID);
        gen.tableSwitch(keys, new TableSwitchGenerator() {
            @Override
            public void generateCase(int key, Label end) {
                CompanionFactory.SyncedMemberInfo info = handles.get(key - firstID);

                gen.getStatic(myType, getMemberID(info.member, READER), mhType);
                gen.loadArg(objectArg);
                gen.checkCast(classToSyncType);
                gen.loadThis();
                gen.loadArg(inStreamArg);
                gen.invokeVirtual(mhType, invokeExact);
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

    private static Method readIDMethod() {
        return getMethod("int readID(de.take_weiland.mods.commons.net.MCDataInput)");
    }

    private void makeReadID() {
        Method method = readIDMethod();
        GeneratorAdapter gen = new GeneratorAdapter(0, method, null, null, cw);

        Method readMethod = new Method("read" + idSize(true), INT_TYPE, new Type[0]);
        gen.loadArg(0);
        gen.invokeInterface(Type.getType(MCDataInput.class), readMethod);
        gen.returnValue();
        gen.endMethod();
    }

    private String idSize(boolean read) {
        int maxId = firstID + handles.size() - 1;
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

        Method writeMethod = new Method("write" + idSize(false), VOID_TYPE, new Type[] { INT_TYPE });
        gen.loadArg(0);
        gen.loadArg(1);
        gen.invokeInterface(Type.getType(MCDataOutput.class), writeMethod);
        gen.returnValue();
        gen.endMethod();
    }

    private void makeCheck() {
        Method method = getMethod("de.take_weiland.mods.commons.net.MCDataOutput check(Object, boolean)");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);

        Type methodHandleType = Type.getType(MethodHandle.class);
        Type mcDataOutType = Type.getType(MCDataOutput.class);
        Type syncTypeType = Type.getType(SyncType.class);
        Type objectType = Type.getType(Object.class);
        Type syncHelpersType = Type.getType(SyncHelpers.class);
        Type myType = Type.getObjectType(className);
        Type holderType = Type.getType(clazz);

        int outStream = gen.newLocal(mcDataOutType);

        if (superClass != SyncCompanion.class) {
            gen.loadThis();
            gen.loadArg(0);
            gen.push(true); // isSuperCall
            gen.invokeConstructor(getObjectType(superName), method);
        } else {
            gen.push((String) null); // don't care about the type
        }
        gen.storeLocal(outStream);

        Label next = null;

        for (int index = 0, len = handles.size(); index < len; index++) {
            CompanionFactory.SyncedMemberInfo info = handles.get(index);

            if (next != null) {
                gen.mark(next);
            }
            next = new Label();

            getMH(gen, info.member, CHECKER);
            gen.loadArg(0);
            gen.checkCast(holderType);
            gen.loadThis();
            gen.invokeVirtual(methodHandleType, new Method("invokeExact", BOOLEAN_TYPE, new Type[]{holderType, myType}));
            gen.ifZCmp(NE, next);

            Label nonNull = new Label();
            gen.loadLocal(outStream);
            gen.ifNonNull(nonNull);

            gen.getStatic(syncTypeType, SyncHelpers.getSyncType(clazz).name(), syncTypeType);
            gen.loadArg(0);
            gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[]{syncTypeType, objectType}));
            gen.storeLocal(outStream);

            gen.mark(nonNull);

            gen.loadThis();
            gen.loadLocal(outStream);
            gen.push(firstID + index);
            gen.invokeVirtual(myType, writeIDMethod());

            getMH(gen, info.member, WRITER);
            gen.loadLocal(outStream);
            gen.loadArg(0);
            gen.checkCast(holderType);
            gen.loadThis();
            gen.invokeVirtual(methodHandleType, new Method("invokeExact", VOID_TYPE, new Type[]{mcDataOutType, holderType, myType}));
        }

        if (next != null) {
            gen.mark(next);
        }
        Label end = new Label();
        gen.loadArg(1);
        gen.ifZCmp(NE, end);
        gen.loadLocal(outStream);
        gen.ifNull(end);

        gen.loadThis();
        gen.loadLocal(outStream);
        gen.push(0);
        gen.invokeVirtual(myType, writeIDMethod());

        gen.getStatic(syncTypeType, SyncHelpers.getSyncType(clazz).name(), syncTypeType);
        gen.loadArg(0);
        gen.loadLocal(outStream);
        gen.invokeStatic(syncHelpersType, new Method("sendStream", VOID_TYPE, new Type[] { syncTypeType, objectType, mcDataOutType }));

        gen.mark(end);
        gen.loadLocal(outStream);
        gen.returnValue();
        gen.endMethod();
    }

    private void getMH(GeneratorAdapter gen, Member member, String role) {
        gen.getStatic(Type.getObjectType(className), getMemberID(member, role), Type.getType(MethodHandle.class));
    }

    private void makeCLInit() {
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);

        Type myType = getObjectType(className);
        Type mhArrType = Type.getType(MethodHandle[].class);
        Type mapType = Type.getType(Map.class);
        Type methodHandleType = Type.getType(MethodHandle.class);

        int map = gen.newLocal(mapType);
        gen.push(myType);
        gen.invokeStatic(Type.getType(BytecodeEmittingCompanionGenerator.class), getMethod("java.util.Map getMHMap(java.lang.Class)"));
        gen.storeLocal(map);


        for (CompanionFactory.SyncedMemberInfo info : handles) {
            gen.loadLocal(map);
            gen.push(getMemberID(info.member));
            gen.invokeInterface(mapType, getMethod("Object get(Object)"));
            gen.checkCast(mhArrType);

            gen.dup();

            gen.push(CHECKER_IN_ARR);
            gen.arrayLoad(methodHandleType);
            gen.putStatic(myType, getMemberID(info.member, CHECKER), methodHandleType);

            gen.dup();

            gen.push(READER_IN_ARR);
            gen.arrayLoad(methodHandleType);
            gen.putStatic(myType, getMemberID(info.member, READER), methodHandleType);

            gen.push(WRITER_IN_ARR);
            gen.arrayLoad(methodHandleType);
            gen.putStatic(myType, getMemberID(info.member, WRITER), methodHandleType);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private static String getMemberID(Member member, String role) {
        return getMemberID(member) + "$" + role;
    }

    private static String getMemberID(Member member) {
        return member.getName() + (member instanceof Field ? "$f" : "$m");
    }

    private Class<?> finish() {
        Class<?> cls;

        // gross hack, please close your eyes
        synchronized (BytecodeEmittingCompanionGenerator.class) {
            // <clinit> of generated class calls getMHMap

            staticHandles = handles;
            try {
                cw.visitEnd();
                cls = SCReflection.defineDynamicClass(cw.toByteArray(), BytecodeEmittingCompanionGenerator.class);
            } finally{
                staticHandles = null;
            }
        }

        return cls;
    }

    private static List<CompanionFactory.SyncedMemberInfo> staticHandles;

    // called from <clinit> in generated classes, see #finish()
    static Map<String, MethodHandle[]> getMHMap(Class<?> companionClass) throws NoSuchFieldException, IllegalAccessException {
        Map<String, MethodHandle[]> map = Maps.newHashMapWithExpectedSize(staticHandles.size());

        for (CompanionFactory.SyncedMemberInfo info : staticHandles) {
            // use reflection so we don't have to specify the (unknown) type of the field
            // fields must also be private to avoid any name-clashes between super and subclasses
            Field companion = companionClass.getDeclaredField(getMemberID(info.member, COMPANION));
            companion.setAccessible(true);
            MethodHandle companionGetter = publicLookup().unreflectGetter(companion);
            MethodHandle companionSetter = publicLookup().unreflectSetter(companion);

            SyncerFactory.Instance instance = info.handle.make(info.getter, info.setter, companionGetter, companionSetter);

            MethodHandle[] arr = new MethodHandle[3];
            arr[CHECKER_IN_ARR] = instance.getChecker();
            arr[READER_IN_ARR] = instance.getReader();
            arr[WRITER_IN_ARR] = instance.getWriter();

            map.put(getMemberID(info.member), arr);
        }

        return map;
    }

}
