package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.SCReflection;
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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.*;
import static org.objectweb.asm.commons.GeneratorAdapter.NE;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
public final class BytecodeEmittingCompanionGenerator {

    private static final String SYNCER = "syncer";

    private static final String COMPANION = "companion";

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
        handles = CompanionFactories.getSyncedMemberInfo(clazz);
        if (handles.isEmpty()) {
            return null;
        }
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
        className = SCReflection.nextDynamicClassName(clazz.getPackage());

        superClass = findAppropriateSuperClass();
        superName = Type.getInternalName(superClass);
        firstID = factory.getNextFreeIDFor(clazz);

        cw = new ClassWriter(COMPUTE_FRAMES);

        cw.visit(V1_7, ACC_PUBLIC, className, null, superName, null);

        Method cstr = getMethod("void <init>()");
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, cstr, null, null, cw);
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
        for (CompanionFactory.SyncedMemberInfo info : handles) {
            String descSyncer = Type.getDescriptor(Syncer.class);

            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, getMemberID(info.member, SYNCER), descSyncer, null, null);

            Class<?> companionType = info.syncer.getCompanionType();
            if (companionType != null) {
                cw.visitField(ACC_PRIVATE, getMemberID(info.member, COMPANION), Type.getDescriptor(companionType), null, null);
            }
        }
    }

    private void makeRead() {
        Method method = getMethod("int read(Object, de.take_weiland.mods.commons.net.MCDataInput)");
        final GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, method, null, null, cw);

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
                boolean hasCompanion = info.syncer.getCompanionType() != null;
                Type companionType = hasCompanion ? Type.getType(info.syncer.getCompanionType()) : null;

                gen.loadArg(objectArg); // for setter
                gen.checkCast(valueHolderType);

                gen.getStatic(myType, getMemberID(info.member, SYNCER), syncerType);
                gen.loadArg(objectArg);
                gen.checkCast(valueHolderType);
                loadMemberVal(gen, info.member);
                if (info.type().isPrimitive()) {
                    box(gen, info.type());
                }

                if (hasCompanion) {
                    gen.loadThis();
                    gen.getField(myType, getMemberID(info.member, COMPANION), companionType);
                    if (info.syncer.getCompanionType().isPrimitive()) {
                        box(gen, info.syncer.getCompanionType());
                    }
                } else {
                    gen.push((String) null); // type doesn't matter
                }
                gen.loadArg(inStreamArg);

                gen.invokeInterface(syncerType, new Method("read", objectType, new Type[]{objectType, objectType, mcDataInType}));

                gen.checkCast(Type.getType(Primitives.wrap(info.type())));
                if (info.type().isPrimitive()) {
                    unbox(gen, info.type());
                }
                storeMemberVal(gen, info);

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

    private static void loadMemberVal(GeneratorAdapter gen, Member member) {
        if (member instanceof Field) {
            gen.getField(Type.getType(member.getDeclaringClass()), member.getName(), Type.getType(((Field) member).getType()));
        } else {
            if (member.getDeclaringClass().isInterface()) {
                gen.invokeInterface(Type.getType(member.getDeclaringClass()), getMethod((java.lang.reflect.Method) member));
            } else {
                gen.invokeVirtual(Type.getType(member.getDeclaringClass()), getMethod((java.lang.reflect.Method) member));
            }
        }
    }

    private static void storeMemberVal(GeneratorAdapter gen, CompanionFactory.SyncedMemberInfo info) {
        if (info.member instanceof Field) {
            gen.putField(Type.getType(info.member.getDeclaringClass()), info.member.getName(), Type.getType(((Field) info.member).getType()));
        } else {
            if (info.setterMethod.getDeclaringClass().isInterface()) {
                gen.invokeInterface(Type.getType(info.setterMethod.getDeclaringClass()), getMethod(info.setterMethod));
            } else {
                gen.invokeVirtual(Type.getType(info.setterMethod.getDeclaringClass()), getMethod(info.setterMethod));
            }
        }
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

        for (int index = 0, len = handles.size(); index < len; index++) {
            CompanionFactory.SyncedMemberInfo info = handles.get(index);

            if (next != null) {
                gen.mark(next);
            }
            next = new Label();

            boolean hasCompanion = info.syncer.getCompanionType() != null;
            Type companionType = hasCompanion ? Type.getType(info.syncer.getCompanionType()) : null;

            gen.getStatic(myType, getMemberID(info.member, SYNCER), syncerType);
            gen.loadArg(objectArg);
            gen.checkCast(valueHolderType);
            loadMemberVal(gen, info.member);
            if (info.type().isPrimitive()) {
                box(gen, info.type());
            }

            if (hasCompanion) {
                gen.loadThis();
                gen.getField(myType, getMemberID(info.member, COMPANION), companionType);
                if (info.syncer.getCompanionType().isPrimitive()) {
                    box(gen, info.syncer.getCompanionType());
                }
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
            gen.getStatic(myType, getMemberID(info.member, SYNCER), syncerType);
            gen.loadArg(objectArg);
            gen.checkCast(valueHolderType);
            loadMemberVal(gen, info.member);
            if (info.type().isPrimitive()) {
                box(gen, info.type());
            }

            if (hasCompanion) {
                gen.loadThis();
                gen.getField(myType, getMemberID(info.member, COMPANION), companionType);
                if (info.syncer.getCompanionType().isPrimitive()) {
                    box(gen, info.syncer.getCompanionType());
                }
            } else {
                gen.push((String) null);
            }
            gen.loadLocal(outStreamID);
            gen.invokeInterface(syncerType, getMethod("Object writeAndUpdate(Object, Object, de.take_weiland.mods.commons.net.MCDataOutput)"));
            if (hasCompanion) {
                gen.checkCast(Type.getType(Primitives.wrap(info.syncer.getCompanionType())));
                if (info.syncer.getCompanionType().isPrimitive()) {
                    unbox(gen, info.syncer.getCompanionType());
                }
                gen.putField(myType, getMemberID(info.member, COMPANION), companionType);
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

    private static void box(GeneratorAdapter gen, Class<?> primitive) {
        Type wrapped = Type.getType(Primitives.wrap(primitive));
        gen.invokeStatic(wrapped, new Method("valueOf", wrapped, new Type[] {  Type.getType(primitive) }));
    }

    private static void unbox(GeneratorAdapter gen, Class<?> primitive) {
        Type wrapped = Type.getType(Primitives.wrap(primitive));
        Type primType = Type.getType(primitive);
        gen.invokeVirtual(wrapped, new Method(primType.getClassName() + "Value", primType, new Type[0]));
    }

    private boolean needCallSuper() {
        return superClass != SyncCompanion.class && superClass != IEEPSyncCompanion.class;
    }

    private void makeCLInit() {
        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);

        Type myType = getObjectType(className);
        Type mapType = Type.getType(Map.class);
        Type syncerType = Type.getType(Syncer.class);

        int map = gen.newLocal(mapType);
        gen.invokeStatic(Type.getType(BytecodeEmittingCompanionGenerator.class), getMethod("java.util.Map getSyncers()"));
        gen.storeLocal(map);

        for (CompanionFactory.SyncedMemberInfo info : handles) {
            gen.loadLocal(map);
            gen.push(getMemberID(info.member));
            gen.invokeInterface(mapType, getMethod("Object get(Object)"));
            gen.checkCast(syncerType);
            gen.putStatic(myType, getMemberID(info.member, SYNCER), syncerType);
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
    public static Map<String, Syncer<?, ?>> getSyncers() throws NoSuchFieldException, IllegalAccessException {
        Map<String, Syncer<?, ?>> map = Maps.newHashMapWithExpectedSize(staticHandles.size());

        for (CompanionFactory.SyncedMemberInfo info : staticHandles) {
            map.put(getMemberID(info.member), info.syncer);
        }

        return map;
    }

}
