package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
final class ASMGetterSetterProperty implements ASMProperty {

    private final Method getter;
    private final Method setter;

    ASMGetterSetterProperty(Method getter, Method setter) {
        this.getter = getter;
        this.setter = setter;
    }


    @Override
    public void loadValue(MethodVisitor mv, Consumer<MethodVisitor> instanceLoader) {
        instanceLoader.accept(mv);
        mv.visitMethodInsn(invokeOpcode(getter), Type.getInternalName(getter.getDeclaringClass()), getter.getName(), Type.getMethodDescriptor(getter), getter.getDeclaringClass().isInterface());
    }

    @Override
    public void setValue(MethodVisitor mv, Consumer<MethodVisitor> valueLoader, Consumer<MethodVisitor> instanceLoader) {
        instanceLoader.accept(mv);
        valueLoader.accept(mv);
        mv.visitMethodInsn(invokeOpcode(setter), Type.getInternalName(setter.getDeclaringClass()), setter.getName(), Type.getMethodDescriptor(setter), setter.getDeclaringClass().isInterface());
    }

    private static int invokeOpcode(Method method) {
        int mod = method.getModifiers();
        if (Modifier.isStatic(mod)) {
            return INVOKESTATIC;
        } else if (Modifier.isPrivate(mod)) {
            return INVOKESPECIAL;
        } else if (method.getDeclaringClass().isInterface()) {
            return INVOKEINTERFACE;
        } else {
            return INVOKEVIRTUAL;
        }
    }
}
