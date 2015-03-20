package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public class PlayerCloneHook extends MethodVisitor {

    public PlayerCloneHook(MethodVisitor mv) {
        super(ASM4, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == RETURN) {
            Type entityPlayerType = Type.getObjectType("net/minecraft/entity/player/EntityPlayer");
            String asmHooks = Type.getInternalName(ASMHooks.class);

            super.visitVarInsn(ALOAD, 1);
            super.visitVarInsn(ALOAD, 0);
            super.visitMethodInsn(INVOKESTATIC, asmHooks, ASMHooks.ON_PLAYER_CLONE, Type.getMethodDescriptor(VOID_TYPE, entityPlayerType, entityPlayerType));
        }

        super.visitInsn(opcode);
    }
}
