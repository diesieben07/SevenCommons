package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicLong;

import static de.take_weiland.mods.commons.util.JavaUtils.unsafe;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
//final class OptimizedPropertyCompiler {
//
//    static PropertyAccess<?> optimize(Field field) {
//        return optimize0(field, null);
//    }
//
//    static PropertyAccess<?> optimize(Method getter, Method setter) {
//        return optimize0(getter, setter);
//    }
//
//    private static PropertyAccess<?> optimize0(Member member, Method setter) {
//        try {
//            return compileClass(member, setter);
//        } catch (ReflectiveOperationException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    static MethodHandle getterMHTemp;
//    static MethodHandle setterMHTemp;
//
//    private static final AtomicLong counter = new AtomicLong();
//
//    private static PropertyAccess<?> compileClass(Member member, Method setter) throws ReflectiveOperationException {
//        String name;
//
//        name = SCReflection.nextDynamicClassName();
//
//        Class<?> declaringClass = member.getDeclaringClass();
////        name = Type.getInternalName(declaringClass) + "$$_sc$prop$" + counter.getAndIncrement();
//
//        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        cw.visit(V1_8, ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, name, null, getSuperclass(), new String[]{Type.getInternalName(PropertyAccess.class)});
//
//        generateConstructor(cw);
//
//        Type myType = Type.getObjectType(name);
//        Type methodHandleType = Type.getType(MethodHandle.class);
//        Type compilerType = Type.getType(OptimizedPropertyCompiler.class);
//
//        Class<?> type;
//        boolean canSet;
//        if (member instanceof Field) {
//            canSet = !Modifier.isFinal(member.getModifiers());
//            type = ((Field) member).getType();
//        } else {
//            canSet = setter != null;
//            type = ((Method) member).getReturnType();
//        }
//
//        Class<?> rawType = type.isPrimitive() ? type : Object.class;
//        Type asmType = Type.getType(type);
//        Type rawAsmType = Type.getType(rawType);
//
//        MethodHandle getMH = null;
//        MethodHandle setMH = null;
//        if (member instanceof Field) {
//            if (!canAccessDirectly(member)) {
//                ((Field) member).setAccessible(true);
//
//                getMH = publicLookup().unreflectGetter((Field) member);
//                if (canSet) {
//                    setMH = publicLookup().unreflectSetter((Field) member);
//                }
//            }
//        } else {
//            if (!canAccessDirectly(member)) {
//                ((AccessibleObject) member).setAccessible(true);
//                getMH = publicLookup().unreflect((Method) member);
//            }
//            if (canSet && !canAccessDirectly(setter)) {
//                setter.setAccessible(true);
//                setMH = publicLookup().unreflect(setter);
//            }
//        }
//
//        if (getMH != null) {
//            getMH = getMH.asType(methodType(rawType, Object.class));
//            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "mh0", methodHandleType.getDescriptor(), null, null);
//        }
//        if (setMH != null) {
//            setMH = setMH.asType(methodType(void.class, Object.class, rawType));
//            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "mh1", methodHandleType.getDescriptor(), null, null);
//        }
//
//        if (getMH != null || setMH != null) {
//            GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
//            gen.visitCode();
//            if (getMH != null) {
//                gen.getStatic(compilerType, "getterMHTemp", methodHandleType);
//                gen.putStatic(myType, "mh0", methodHandleType);
//            }
//            if (setMH != null) {
//                gen.getStatic(compilerType, "setterMHTemp", methodHandleType);
//                gen.putStatic(myType, "mh1", methodHandleType);
//            }
//
//            gen.returnValue();
//            gen.endMethod();
//        }
//
//        org.objectweb.asm.commons.Method getMethod;
//        if (type.isPrimitive()) {
//            getMethod = new org.objectweb.asm.commons.Method("get" + StringUtils.capitalize(type.getSimpleName()), asmType, new Type[]{Type.getType(Object.class)});
//        } else {
//            getMethod = getMethod("Object get(Object)");
//        }
//
//        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, getMethod, null, null, cw);
//        gen.visitCode();
//        if (getMH != null) {
//            Type rawReturnType = Type.getType(getMH.type().returnType());
//            Type objectType = Type.getType(Object.class);
//
//            gen.push();
//            gen.getStatic(myType, "mh0", methodHandleType);
//            gen.loadArg(0);
//
//            gen.invokeVirtual(methodHandleType, new org.objectweb.asm.commons.Method("invokeExact", rawReturnType, new Type[]{objectType}));
//        } else {
//            gen.loadArg(0);
//            Class<?> ownerClass = member.getDeclaringClass();
//            castIfNeeded(gen, ownerClass);
//            if (member instanceof Field) {
//                gen.getField(Type.getType(ownerClass), member.getName(), Type.getType(((Field) member).getType()));
//            } else {
//                if (ownerClass.isInterface()) {
//                    gen.invokeInterface(Type.getType(ownerClass), getMethod((Method) member));
//                } else {
//                    gen.invokeVirtual(Type.getType(ownerClass), getMethod((Method) member));
//                }
//            }
//        }
//        gen.returnValue();
//        gen.endMethod();
//
//        org.objectweb.asm.commons.Method setMethod;
//        if (type.isPrimitive()) {
//            setMethod = new org.objectweb.asm.commons.Method("set" + StringUtils.capitalize(type.getSimpleName()), Type.VOID_TYPE, new Type[]{Type.getType(Object.class), asmType});
//        } else {
//            setMethod = getMethod("void set(Object, Object)");
//        }
//
//        gen = new GeneratorAdapter(ACC_PUBLIC, setMethod, null, null, cw);
//        gen.visitCode();
//        if (!canSet) {
//            gen.throwException(Type.getType(UnsupportedOperationException.class), "Immutable Property");
//        } else {
//            if (setMH != null) {
//                Type objectType = Type.getType(Object.class);
//
//                gen.getStatic(myType, "mh1", methodHandleType);
//                gen.loadArg(0);
//                gen.loadArg(1);
//                gen.invokeVirtual(methodHandleType, new org.objectweb.asm.commons.Method("invokeExact", Type.VOID_TYPE, new Type[]{objectType, rawAsmType}));
//            } else {
//                boolean isField = member instanceof Field;
//                Class<?> p0 = (isField ? member : setter).getDeclaringClass();
//                Class<?> p1 = isField ? ((Field) member).getType() : setter.getParameterTypes()[0];
//
//                gen.loadArg(0);
//                castIfNeeded(gen, p0);
//                gen.loadArg(1);
//                castIfNeeded(gen, p1);
//
//                if (isField) {
//                    gen.putField(Type.getType(p0), member.getName(), Type.getType(p1));
//                } else {
//                    if (p0.isInterface()) {
//                        gen.invokeInterface(Type.getType(p0), getMethod(setter));
//                    } else {
//                        gen.invokeVirtual(Type.getType(p0), getMethod(setter));
//                    }
//                }
//            }
//
//            gen.returnValue();
//        }
//        gen.endMethod();
//
//        if (type.isPrimitive()) {
//            gen = new GeneratorAdapter(ACC_PUBLIC, getMethod("Object get(Object)"), null, null, cw);
//            gen.visitCode();
//            gen.loadThis();
//            gen.loadArg(0);
//            gen.invokeVirtual(myType, getMethod);
//            ASMUtils.convertTypes(gen, type, Object.class);
//            gen.returnValue();
//            gen.endMethod();
//
//            gen = new GeneratorAdapter(ACC_PUBLIC, getMethod("void set(Object, Object)"), null, null, cw);
//            gen.visitCode();
//            gen.loadThis();
//            gen.loadArg(0);
//            gen.loadArg(1);
//            ASMUtils.convertTypes(gen, Object.class, type);
//            gen.invokeVirtual(myType, setMethod);
//            gen.returnValue();
//            gen.endMethod();
//        }
//
//        cw.visitEnd();
//
//        synchronized (PropertyAccess.class) {
//            getterMHTemp = getMH;
//            setterMHTemp = setMH;
//            try {
//                Class<?> clazz = SCReflection.defineAnonymousClass(cw.toByteArray(), declaringClass);
//                return (PropertyAccess<?>) unsafe().allocateInstance(clazz);
//            } finally {
//                getterMHTemp = setterMHTemp = null;
//            }
//        }
//    }
//
//    private static final String magicAccessorClass = "sun/reflect/MagicAccessorImpl";
//    private static final boolean enableDirectInvokePrivate;
//
//    static {
//        boolean hasMagicAccessor = false;
//        try {
//            Class.forName(magicAccessorClass.replace('/', '.'));
//            hasMagicAccessor = true;
//        } catch (ClassNotFoundException x) {
//            // ignored
//        }
//        enableDirectInvokePrivate = true;
//    }
//
//    private static String getSuperclass() {
//        return "java/lang/Object";
//    }
//
//    private static boolean canAccessDirectly(Member member) {
//        if (enableDirectInvokePrivate) {
//            return true;
//        }
//        boolean a = Modifier.isPublic(member.getDeclaringClass().getModifiers());
//        boolean b = Modifier.isPublic(member.getModifiers());
//        return a && b;
//    }
//
//    private static void castIfNeeded(GeneratorAdapter gen, Class<?> target) {
//        if (!target.isPrimitive() && target != Object.class && !target.isInterface()) {
//            gen.checkCast(Type.getType(target));
//        }
//    }
//
//    private static void generateConstructor(ClassWriter cw) {
//        GeneratorAdapter gen = new GeneratorAdapter(0, getMethod("void <init>()"), null, null, cw);
//        gen.visitCode();
//        gen.loadThis();
//        gen.invokeConstructor(Type.getObjectType(getSuperclass()), getMethod("void <init>()"));
//        gen.returnValue();
//        gen.endMethod();
//    }
//
//    private OptimizedPropertyCompiler() {
//    }
//
//}
