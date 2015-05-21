package de.take_weiland.mods.commons.internal.prop;

import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.*;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.commons.Method.getMethod;

/**
 * @author diesieben07
 */
final class OptimizedPropertyCompiler {

    static PropertyAccess<?> optimize(Field field) {
        return optimize0(field, null);
    }

    static PropertyAccess<?> optimize(Method getter, Method setter) {
        return optimize0(getter, setter);
    }

    private static PropertyAccess<?> optimize0(Member member, Method setter) {
        try {
            return compileClass(member, setter);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    static MethodHandle getterMHTemp;
    static MethodHandle setterMHTemp;

    private static PropertyAccess<?> compileClass(Member member, Method setter) throws ReflectiveOperationException {
        String name = SCReflection.nextDynamicClassName(OptimizedPropertyCompiler.class.getPackage());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_FINAL, name, null, "java/lang/Object", new String[]{Type.getInternalName(PropertyAccess.class)});

        generateConstructor(cw);

        Type myType = Type.getObjectType(name);
        Type methodHandleType = Type.getType(MethodHandle.class);
        Type compilerType = Type.getType(OptimizedPropertyCompiler.class);

        boolean canSet;
        if (member instanceof Field) {
            canSet = !Modifier.isFinal(member.getModifiers());
        } else {
            canSet = setter != null;
        }

        MethodHandle getMH = null;
        MethodHandle setMH = null;
        if (member instanceof Field) {
            if (!canAccessDirectly(member)) {
                ((Field) member).setAccessible(true);

                getMH = publicLookup().unreflectGetter((Field) member);
                if (canSet) {
                    setMH = publicLookup().unreflectSetter((Field) member);
                }
            }
        } else {
            if (!canAccessDirectly(member)) {
                ((AccessibleObject) member).setAccessible(true);
                getMH = publicLookup().unreflect((Method) member).asType(methodType(Object.class, Object.class));
            }
            if (canSet && !canAccessDirectly(setter)) {
                setter.setAccessible(true);
                setMH = publicLookup().unreflect(setter);
            }
        }

        if (getMH != null) {
            getMH = getMH.asType(methodType(Object.class, Object.class));
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "mh0", methodHandleType.getDescriptor(), null, null);
        }
        if (setMH != null) {
            setMH = setMH.asType(methodType(void.class, Object.class, Object.class));
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "mh1", methodHandleType.getDescriptor(), null, null);
        }

        if (getMH != null || setMH != null) {
            GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC | ACC_STATIC, getMethod("void <clinit>()"), null, null, cw);
            gen.visitCode();
            if (getMH != null) {
                gen.getStatic(compilerType, "getterMHTemp", methodHandleType);
                gen.putStatic(myType, "mh0", methodHandleType);
            }
            if (setMH != null) {
                gen.getStatic(compilerType, "setterMHTemp", methodHandleType);
                gen.putStatic(myType, "mh1", methodHandleType);
            }

            gen.returnValue();
            gen.endMethod();
        }

        GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC, getMethod("Object get(Object)"), null, null, cw);
        gen.visitCode();
        if (getMH != null) {
            Type objectType = Type.getType(Object.class);

            gen.getStatic(myType, "mh0", methodHandleType);
            gen.loadArg(0);

            gen.invokeVirtual(methodHandleType, new org.objectweb.asm.commons.Method("invokeExact", objectType, new Type[]{objectType}));
        } else {
            gen.loadArg(0);
            Class<?> ownerClass = member.getDeclaringClass();
            castIfNeeded(gen, ownerClass);
            if (member instanceof Field) {
                gen.getField(Type.getType(ownerClass), member.getName(), Type.getType(((Field) member).getType()));
            } else {
                if (ownerClass.isInterface()) {
                    gen.invokeInterface(Type.getType(ownerClass), getMethod((Method) member));
                } else {
                    gen.invokeVirtual(Type.getType(ownerClass), getMethod((Method) member));
                }
            }
        }
        gen.returnValue();
        gen.endMethod();

        gen = new GeneratorAdapter(ACC_PUBLIC, getMethod("void set(Object, Object)"), null, null, cw);
        gen.visitCode();
        if (!canSet) {
            gen.throwException(Type.getType(UnsupportedOperationException.class), "Cannot set final property");
        } else {
            if (setMH != null) {
                Type objectType = Type.getType(Object.class);

                gen.getStatic(myType, "mh1", methodHandleType);
                gen.loadArg(0);
                gen.loadArg(1);
                gen.invokeVirtual(methodHandleType, new org.objectweb.asm.commons.Method("invokeExact", Type.VOID_TYPE, new Type[]{objectType, objectType}));
            } else {
                boolean isField = member instanceof Field;
                Class<?> p0 = (isField ? member : setter).getDeclaringClass();
                Class<?> p1 = isField ? ((Field) member).getType() : setter.getParameterTypes()[0];

                gen.loadArg(0);
                castIfNeeded(gen, p0);
                gen.loadArg(1);
                castIfNeeded(gen, p1);

                if (isField) {
                    gen.putField(Type.getType(p0), member.getName(), Type.getType(p1));
                } else {
                    if (p0.isInterface()) {
                        gen.invokeInterface(Type.getType(p0), new org.objectweb.asm.commons.Method(setter.getName(), Type.getMethodDescriptor(setter)));
                    } else {
                        gen.invokeVirtual(Type.getType(p0), new org.objectweb.asm.commons.Method(setter.getName(), Type.getMethodDescriptor(setter)));
                    }
                }
            }

            gen.returnValue();
        }
        gen.endMethod();
        cw.visitEnd();

        synchronized (PropertyAccess.class) {
            getterMHTemp = getMH;
            setterMHTemp = setMH;
            try {
                return (PropertyAccess<?>) SCReflection.defineDynamicClass(cw.toByteArray()).newInstance();
            } finally {
                getterMHTemp = setterMHTemp = null;
            }
        }
    }

    private static boolean canAccessDirectly(Member member) {
        boolean a = Modifier.isPublic(member.getDeclaringClass().getModifiers());
        boolean b = Modifier.isPublic(member.getModifiers());
        return a && b;
    }

    private static void castIfNeeded(GeneratorAdapter gen, Class<?> target) {
        if (target != Object.class && !target.isInterface()) {
            gen.checkCast(Type.getType(target));
        }
    }

    private static void generateConstructor(ClassWriter cw) {
        GeneratorAdapter gen = new GeneratorAdapter(0, getMethod("void <init>()"), null, null, cw);
        gen.visitCode();
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class), getMethod("void <init>()"));
        gen.returnValue();
        gen.endMethod();
    }

}
