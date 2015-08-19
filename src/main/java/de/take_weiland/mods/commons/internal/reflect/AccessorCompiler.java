package de.take_weiland.mods.commons.internal.reflect;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

import static de.take_weiland.mods.commons.util.JavaUtils.unsafe;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 * @author diesieben07
 */
public class AccessorCompiler {

    public static PropertyAccess<?> makeOptimizedProperty(Member member) {
        Triple<Class<?>, MethodHandle, MethodHandle> resolved = resolve(member);
        CompileContext context = emitStart(Type.getInternalName(PropertyAccess.class));

        emitDelegateMethod(context, "get", methodType(Object.class, Object.class), resolved.getMiddle());
        emitDelegateMethod(context, "set", methodType(void.class, Object.class, Object.class), resolved.getRight());

        Class<?> actualType = resolved.getLeft();
        if (actualType.isPrimitive()) {
            String postfix = StringUtils.capitalize(actualType.getName());
            emitDelegateMethod(context, "get" + postfix, resolved.getMiddle());
            emitDelegateMethod(context, "set" + postfix, resolved.getRight());
        }

        Class<?> clazz = context.link(AccessorCompiler.class);
        try {
            return (PropertyAccess<?>) unsafe().allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Triple<Class<?>, MethodHandle, MethodHandle> resolve(Member member) {
        ((AccessibleObject) member).setAccessible(true);

        Class<?> type = getType(member);
        Class<?> actualType = getRawType(type);

        MethodHandle getter;
        MethodHandle setter;
        try {
            if (member instanceof Field) {
                Field field = (Field) member;
                getter = publicLookup().unreflectGetter(field);

                if (Modifier.isFinal(field.getModifiers())) {
                    setter = MH_INVALID_SETTER;
                } else {
                    setter = publicLookup().unreflectSetter(field);
                }
            } else {
                Method method = (Method) member;
                getter = publicLookup().unreflect(method);

                Method reflSetter = SCReflection.findSetter(method);
                if (reflSetter == null) {
                    setter = MH_INVALID_SETTER;
                } else {
                    reflSetter.setAccessible(true);
                    setter = publicLookup().unreflect(reflSetter);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("impossible", e);
        }

        getter = getter.asType(methodType(actualType, Object.class));
        setter = setter.asType(methodType(void.class, Object.class, actualType));

        return Triple.of(actualType, getter, setter);
    }

    private static Class<?> getType(Member member) {
        return member instanceof Field ? ((Field) member).getType() : ((Method) member).getReturnType();
    }

    private static Class<?> getRawType(Class<?> clazz) {
        return clazz.isPrimitive() ? clazz : Object.class;
    }

    private static final MethodHandle MH_INVALID_SETTER;

    static {
        try {
            MH_INVALID_SETTER = lookup().findStatic(AccessorCompiler.class, "invalidSetterImpl", methodType(void.class, Object.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void invalidSetterImpl(Object o, Object val) {
        throw new UnsupportedOperationException("Tried to set unmodifiable property");
    }

    private static CompileContext emitStart(String... interfaces) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        String name = SCReflection.nextDynamicClassName();
        String superName = Type.getInternalName(Object.class);
        String cstrDesc = Type.getMethodDescriptor(Type.VOID_TYPE);
        String cstrName = "<init>";

        cw.visit(V1_8, ACC_FINAL, name, null, superName, interfaces);
        MethodVisitor mv = cw.visitMethod(0, cstrName, cstrDesc, null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, superName, cstrName, cstrDesc, false);
        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();


        return new CompileContext(cw);
    }

    private static void emitDelegateMethod(CompileContext context, String name, MethodHandle target) {
        emitDelegateMethod(context, name, target.type(), target);
    }

    private static void emitDelegateMethod(CompileContext context, String name, MethodType targetType, MethodHandle target) {
        Type asmReturnType = Type.getType(targetType.returnType());
        Type[] asmParamTypes = new Type[targetType.parameterCount()];
        for (int i = 0; i < targetType.parameterCount(); i++) {
            asmParamTypes[i] = Type.getType(targetType.parameterType(i));
        }
        String desc = Type.getMethodDescriptor(asmReturnType, asmParamTypes);

        MethodVisitor mv = context.cw.visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
        mv.visitCode();

        emitDelegateCode(context, mv, targetType, target);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitDelegateCode(CompileContext context, MethodVisitor mv, MethodType methodType, MethodHandle target) {
        MethodType targetType = target.type();

        String mhIName = Type.getInternalName(MethodHandle.class);

        context.pushAsConstant(mv, target);
        mv.visitTypeInsn(CHECKCAST, mhIName);

        int varSlot = 1;
        int idx = 0;
        for (Class<?> param : methodType.parameterArray()) {
            Type asmType = Type.getType(param);

            mv.visitVarInsn(asmType.getOpcode(ILOAD), varSlot);
            ASMUtils.convertTypes(mv, asmType, Type.getType(targetType.parameterType(idx)));

            varSlot += asmType.getSize();
            idx++;
        }

        String desc = ASMUtils.getMethodDescriptor(targetType);
        mv.visitMethodInsn(INVOKEVIRTUAL, mhIName, "invokeExact", desc, false);

        Type asmReturnType = Type.getType(methodType.returnType());
        ASMUtils.convertTypes(mv, Type.getType(targetType.returnType()), asmReturnType);
        mv.visitInsn(asmReturnType.getOpcode(IRETURN));
    }

}
