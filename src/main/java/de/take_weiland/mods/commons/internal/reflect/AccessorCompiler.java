package de.take_weiland.mods.commons.internal.reflect;

import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.asm.ClassInfoClassWriter;
import de.take_weiland.mods.commons.reflect.Property;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.reflect.SCReflection;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class AccessorCompiler {

    public static <T> PropertyAccess<T> makeOptimizedProperty(Member member, Property<T> property) {
        ResolvedMember resolved = resolve(member);
        CompileContextImpl context = emitStart(Type.getInternalName(PropertyAccess.class));

        emitDelegateMethod(context, "get", methodType(Object.class, Object.class), resolved.get);
        emitDelegateMethod(context, "set", methodType(void.class, Object.class, Object.class), resolved.set);

        if (resolved.actualType.isPrimitive()) {
            String postfix = StringUtils.capitalize(resolved.actualType.getName());
            emitDelegateMethod(context, "get" + postfix, resolved.get);
            emitDelegateMethod(context, "set" + postfix, resolved.set);
        }

        MethodVisitor mv = context.cw().visitMethod(ACC_PUBLIC, "original", Type.getMethodDescriptor(Type.getType(Property.class)), null, null);
        mv.visitCode();
        context.pushAsConstant(mv, property, Property.class);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        Class<?> clazz = context.link();
        try {
            Constructor<?> cstr = clazz.getDeclaredConstructor();
            cstr.setAccessible(true);
            //noinspection unchecked
            return (PropertyAccess<T>) cstr.newInstance();
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResolvedMember resolve(Member member) {
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

        return new ResolvedMember(actualType, getter, setter);
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

    @SuppressWarnings("unused")
    private static void invalidSetterImpl(Object o, Object val) {
        throw new UnsupportedOperationException("Tried to set unmodifiable property");
    }

    static CompileContextImpl emitStart(String... interfaces) {
        ClassWriter cw = new ClassInfoClassWriter(ClassWriter.COMPUTE_FRAMES);

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


        return new CompileContextImpl(name, cw);
    }

    static void emitDelegateMethod(CompileContextImpl context, String name, MethodHandle target) {
        emitDelegateMethod(context, name, target.type(), target);
    }

    static void emitDelegateMethod(CompileContextImpl context, String name, MethodType methodType, MethodHandle target) {
        Type asmReturnType = Type.getType(methodType.returnType());
        Type[] asmParamTypes = new Type[methodType.parameterCount()];
        for (int i = 0; i < methodType.parameterCount(); i++) {
            asmParamTypes[i] = Type.getType(methodType.parameterType(i));
        }
        String desc = Type.getMethodDescriptor(asmReturnType, asmParamTypes);

        MethodVisitor mv = context.cw().visitMethod(ACC_PUBLIC | ACC_FINAL, name, desc, null, null);
        mv.visitCode();

        emitDelegateCode(context, mv, methodType, target);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitDelegateCode(CompileContextImpl context, MethodVisitor mv, MethodType methodType, MethodHandle target) {
        MethodType targetType = target.type();

        String mhIName = Type.getInternalName(MethodHandle.class);

        context.pushAsConstant(mv, target, MethodHandle.class);

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

    private static final class ResolvedMember {

        final Class<?> actualType;
        final MethodHandle get;
        final MethodHandle set;

        private ResolvedMember(Class<?> actualType, MethodHandle get, MethodHandle set) {
            this.actualType = actualType;
            this.get = get;
            this.set = set;
        }

    }

}
