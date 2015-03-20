package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public final class InitGuiHook extends MethodVisitor {

    public InitGuiHook(MethodVisitor mv) {
        super(ASM4, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        super.visitMethodInsn(opcode, owner, name, desc);
        if (name.equals(MCPNames.method(MCPNames.M_INIT_GUI))) {
            super.visitVarInsn(ALOAD, 0);

            String asmHooks = Type.getInternalName(ASMHooks.class);
            Type guiScreenType = Type.getObjectType("net/minecraft/client/gui/GuiScreen");

            super.visitMethodInsn(INVOKESTATIC, asmHooks, ASMHooks.ON_GUI_INIT, Type.getMethodDescriptor(VOID_TYPE, guiScreenType));
        }
    }
}
