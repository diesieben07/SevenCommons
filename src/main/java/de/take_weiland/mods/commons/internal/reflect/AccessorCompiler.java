package de.take_weiland.mods.commons.internal.reflect;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class AccessorCompiler {

    static void emitDelegate(CompileContext context, MethodVisitor mv, MethodHandle target) {
        String mhIName = Type.getInternalName(MethodHandle.class);

        context.pushAsConstant(mv, target);
        mv.visitTypeInsn(CHECKCAST, mhIName);

        MethodType type = target.type();
        Type[] asmParamTypes = new Type[type.parameterCount()];
        Type asmReturnType = Type.getType(type.returnType());

        int varSlot = 1;
        int idx = 0;
        for (Class<?> param : type.parameterList()) {
            Type asmType = Type.getType(param);
            asmParamTypes[idx] = asmType;
            mv.visitVarInsn(asmType.getOpcode(ILOAD), varSlot);
            varSlot += asmType.getSize();
        }

        String desc = Type.getMethodDescriptor(asmReturnType, asmParamTypes);
        mv.visitMethodInsn(INVOKEVIRTUAL, mhIName, "invokeExact", desc, false);
        mv.visitInsn(asmReturnType.getOpcode(IRETURN));
    }

}
