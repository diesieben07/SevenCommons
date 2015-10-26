package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.client.SlotDrawHooks;
import de.take_weiland.mods.commons.internal.SRGConstants;
import net.minecraft.inventory.Slot;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class ContainerSlotDrawHook extends ClassVisitor {

    public ContainerSlotDrawHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (MCPNames.method(SRGConstants.M_DRAW_SLOT).equals(name)) {
            mv = new DrawSlotPatcher(mv);
        }
        return mv;
    }

    private static class DrawSlotPatcher extends MethodVisitor {
        DrawSlotPatcher(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            Label endHook = new Label();
            super.visitVarInsn(ALOAD, 0);
            super.visitTypeInsn(INSTANCEOF, Type.getInternalName(SlotDrawHooks.class));
            super.visitJumpInsn(IFEQ, endHook);

            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(ALOAD, 1);
            String name = "preDraw";
            String desc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(Slot.class));
            super.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(SlotDrawHooks.class), name, desc, true);
            super.visitJumpInsn(IFNE, endHook);
            super.visitInsn(RETURN);
            super.visitLabel(endHook);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                Label endHook = new Label();
                super.visitVarInsn(ALOAD, 0);
                super.visitTypeInsn(INSTANCEOF, Type.getInternalName(SlotDrawHooks.class));
                super.visitJumpInsn(IFEQ, endHook);

                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 1);
                String name = "postDraw";
                String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Slot.class));
                super.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(SlotDrawHooks.class), name, desc, true);
                super.visitLabel(endHook);
            }

            super.visitInsn(opcode);
        }

    }
}
