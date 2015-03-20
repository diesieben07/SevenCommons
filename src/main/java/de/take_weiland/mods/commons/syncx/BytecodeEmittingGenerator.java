package de.take_weiland.mods.commons.syncx;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.sync.SyncType;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.SCReflection;
import net.minecraft.tileentity.TileEntityBeacon;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
final class BytecodeEmittingGenerator implements CompanionGenerator {

    private static final String COMPANION = "companion";
    private static final String CHECKER = "checker";
    private static final int CHECKER_IN_ARR = 0;

    private static final String READER = "reader";
    private static final int READER_IN_ARR = 1;

    private static final String WRITER = "writer";
    private static final int WRITER_IN_ARR = 2;

    private final Class<?> clazz;

    private String className;
    private String superName;
    private ClassWriter cw;
    private List<SyncedMemberInfo> handles;

    public static void main(String[] args) throws NoSuchFieldException {
        Member member = String.class.getDeclaredField("value");
        Member member2 = String.class.getDeclaredField("hash");
        SyncerFactory.Handle handle = new SyncerFactory.Handle() {
            @Override
            public Class<?> getCompanionType() {
                return int.class;
            }

            @Override
            public SyncerFactory.Instance make(MethodHandle getter, MethodHandle setter, MethodHandle companionGet, MethodHandle companionSet) {
                return new SyncerFactory.Instance(getter, getter, getter);
            }
        };

        List<SyncedMemberInfo> infoList = ImmutableList.of(
                new SyncedMemberInfo(member, handle),
                new SyncedMemberInfo(member2, handle)
        );
        new BytecodeEmittingGenerator(TileEntityBeacon.class, infoList).generateCompanionConstructor();
    }


    BytecodeEmittingGenerator(Class<?> clazz, List<SyncedMemberInfo> handles) {
        this.clazz = clazz;
        this.handles = handles;
    }

    @Override
    public MethodHandle generateCompanionConstructor() {
        beginClass();

        makeMHFields();
        makeCLInit();
        makeRead();
        makeCheck();

        Class<?> clazz = finish();
        try {
            // constructors are package private so we can access them
            return lookup().findConstructor(clazz, methodType(void.class)).asType(methodType(SyncerCompanion.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Internal Error during companion generation", e);
        }
    }

    private void beginClass() {
        className = SCReflection.nextDynamicClassName(BytecodeEmittingGenerator.class.getPackage());

        superName = Type.getInternalName(SyncerCompanion.class);
        cw = new ClassWriter(COMPUTE_FRAMES);
        cw.visit(V1_7, ACC_PUBLIC, className, null, superName, null);

        Method cstr = getMethod("void <init>()");
        GeneratorAdapter gen = new GeneratorAdapter(0, cstr, null, null, cw);
        gen.loadThis();
        gen.invokeConstructor(Type.getObjectType(superName), cstr);
        gen.returnValue();
        gen.endMethod();
    }

    private void makeMHFields() {
        for (SyncedMemberInfo info : handles) {
            String descMH = Type.getDescriptor(MethodHandle.class);
            String descCompanion = Type.getDescriptor(info.handle.getCompanionType());

            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, CHECKER), descMH, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, READER), descMH, null, null);
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, WRITER), descMH, null, null);
            cw.visitField(ACC_PRIVATE, getMemberID(info.member, COMPANION), descCompanion, null, null);
        }
    }

    private void makeRead() {
        Method method = getMethod("void read(Object, de.take_weiland.mods.commons.net.MCDataInput)");
        GeneratorAdapter gen = new GeneratorAdapter(0, method, null, null, cw);
        if (!superName.equals(Type.getInternalName(SyncerCompanion.class))) {
            gen.loadThis();
            gen.loadArg(0);
            gen.loadArg(1);
            gen.invokeConstructor(Type.getObjectType(superName), method);
        }

        Type myType = Type.getObjectType(className);
        Type mhType = Type.getType(MethodHandle.class);
        Type mcDataInType = Type.getType(MCDataInput.class);
        Type classToSyncType = Type.getType(clazz);
        Method invokeExact = new Method("invokeExact", VOID_TYPE, new Type[] { classToSyncType, myType, mcDataInType });

        for (SyncedMemberInfo info : handles) {
            gen.getStatic(myType, getMemberID(info.member, READER), mhType);
            gen.loadArg(0);
            gen.checkCast(classToSyncType);
            gen.loadThis();
            gen.loadArg(1);
            gen.invokeVirtual(mhType, invokeExact);
        }

        gen.returnValue();
        gen.endMethod();
    }

    private void makeCheck() {
        Method method = getMethod("de.take_weiland.mods.commons.net.MCDataOutput check(Object, boolean)");
        GeneratorAdapter gen = new GeneratorAdapter(0, method, null, null, cw);

        Type methodHandleType = Type.getType(MethodHandle.class);
        Type mcDataOutType = Type.getType(MCDataOutput.class);
        Type syncTypeType = Type.getType(SyncType.class);
        Type objectType = Type.getType(Object.class);
        Type syncHelpersType = Type.getType(SyncHelpers.class);
        Type myType = Type.getObjectType(className);
        Type holderType = Type.getType(clazz);

        int outStream = gen.newLocal(mcDataOutType);
        if (!superName.equals(Type.getInternalName(SyncerCompanion.class))) {
            gen.loadThis();
            gen.loadArg(0);
            gen.push(false);
            gen.invokeConstructor(getObjectType(superName), method);
        } else {
            gen.push((String) null); // don't care about the type
        }
        gen.storeLocal(outStream);

        Label next = null;

        for (SyncedMemberInfo info : handles) {
            if (next != null) {
                gen.mark(next);
            }
            next = new Label();

            getMH(gen, info.member, CHECKER);
            gen.loadArg(0);
            gen.checkCast(holderType);
            gen.loadThis();
            gen.invokeVirtual(methodHandleType, new Method("invokeExact", BOOLEAN_TYPE, new Type[] { holderType, myType }));
            gen.ifZCmp(NE, next);

            Label nonNull = new Label();
            gen.loadLocal(outStream);
            gen.ifNonNull(nonNull);

            gen.getStatic(syncTypeType, SyncHelpers.getSyncType(clazz).name(), syncTypeType);
            gen.loadArg(0);
            gen.invokeStatic(syncHelpersType, new Method("newOutStream", mcDataOutType, new Type[]  { syncTypeType, objectType }));
            gen.storeLocal(outStream);

            gen.mark(nonNull);
            getMH(gen, info.member, WRITER);
            gen.loadLocal(outStream);
            gen.loadArg(0);
            gen.checkCast(holderType);
            gen.loadThis();
            gen.invokeVirtual(methodHandleType, new Method("invokeExact", VOID_TYPE, new Type[] { mcDataOutType, holderType, myType }));
        }

        if (next != null) {
            gen.mark(next);
        }
        Label end = new Label();
        gen.loadArg(1);
        gen.ifZCmp(NE, end);
        gen.loadLocal(outStream);
        gen.ifNull(end);

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
        gen.invokeStatic(Type.getType(BytecodeEmittingGenerator.class), getMethod("java.util.Map getMHMap(java.lang.Class)"));
        gen.storeLocal(map);


        for (SyncedMemberInfo info : handles) {
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
        synchronized (BytecodeEmittingGenerator.class) {
            // <clinit> of generated class calls getMHMap

            staticHandles = handles;
            try {
                cw.visitEnd();
                cls = SCReflection.defineDynamicClass(cw.toByteArray(), BytecodeEmittingGenerator.class);
            } finally{
                staticHandles = null;
            }
        }

        return cls;
    }

    private static List<SyncedMemberInfo> staticHandles;

    // called from <clinit> in generated classes, see #finish()
    static Map<String, MethodHandle[]> getMHMap(Class<?> companionClass) throws NoSuchFieldException, IllegalAccessException {
        Map<String, MethodHandle[]> map = Maps.newHashMapWithExpectedSize(staticHandles.size());

        for (SyncedMemberInfo info : staticHandles) {
            // use reflection so we don't have to specify the (unknown) type of the field
            Field companion = companionClass.getDeclaredField(getMemberID(info.member, COMPANION));
            companion.setAccessible(true);
            MethodHandle companionGetter = publicLookup().unreflectGetter(companion);
            MethodHandle companionSetter = publicLookup().unreflectSetter(companion);

            SyncerFactory.Instance instance = info.handle.make(info.getter, info.setter, companionGetter, companionSetter);

            MethodHandle[] arr = new MethodHandle[3];
            arr[CHECKER_IN_ARR] = instance.checker;
            arr[READER_IN_ARR] = instance.reader;
            arr[WRITER_IN_ARR] = instance.writer;

            map.put(getMemberID(info.member), arr);
        }

        return map;
    }

}
