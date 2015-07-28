package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class SaveWorldsEventHook extends MethodVisitor {

    private boolean done;

    SaveWorldsEventHook(MethodVisitor mv) {
        super(ASM5, mv);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label);
        if (!done && opcode == IFNE) {
            super.visitVarInsn(ILOAD, 1);
            super.visitMethodInsn(INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.FIRE_WORLD_SAVE,
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.BOOLEAN_TYPE), false);
            done = true;
        }
    }
}
