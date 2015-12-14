package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.SRGConstants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static de.take_weiland.mods.commons.internal.SRGConstants.M_HAS_OVERRIDE_BLOCK_TEXTURE;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class RenderBlocksHook extends ClassVisitor {

    RenderBlocksHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (name.equals(MCPNames.method(SRGConstants.M_RENDER_BLOCK_AS_ITEM))) {
            return new RenderAsItemHook(mv);
        }

        int side = getSide(name);
        if (side != -1) {
            mv = new RenderSideMethodHook(mv, side);
        }

        return mv;
    }

    private static int getSide(String method) {
        if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_Y_NEG))) {
            return 0;
        } else if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_Y_POS))) {
            return 1;
        } else if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_Z_NEG))) {
            return 2;
        } else if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_Z_POS))) {
            return 3;
        } else if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_X_NEG))) {
            return 4;
        } else if (method.equals(MCPNames.method(SRGConstants.M_RENDER_FACE_X_POS))) {
            return 5;
        } else {
            return -1;
        }
    }

    private static final class RenderAsItemHook extends MethodVisitor {

        RenderAsItemHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            super.visitInsn(ICONST_1);
            putField();
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN || opcode == ATHROW) {
                super.visitInsn(ICONST_0);
                putField();

            }
            super.visitInsn(opcode);
        }

        private void putField() {
            super.visitFieldInsn(PUTSTATIC, ASMHooks.CLASS_NAME, ASMHooks.DRAW_BLOCK_INV, Type.BOOLEAN_TYPE.getDescriptor());
        }
    }

    private static final class RenderSideMethodHook extends MethodVisitor {

        private final int side;

        private static final int SEARCH_CALL_OVERRIDE = 0, SEARCH_JMP = 1,
                SEARCH_JMP_TARGET                     = 2, DONE = 3;

        private int stage = SEARCH_CALL_OVERRIDE;
        private Label jumpTarget;

        RenderSideMethodHook(MethodVisitor mv, int side) {
            super(ASM5, mv);
            this.side = side;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (stage == SEARCH_CALL_OVERRIDE && opcode == INVOKEVIRTUAL && name.equals(MCPNames.method(M_HAS_OVERRIDE_BLOCK_TEXTURE))) {
                stage = SEARCH_JMP;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            if (stage == SEARCH_JMP && opcode == IFEQ) {
                jumpTarget = label;
                stage = SEARCH_JMP_TARGET;
            }
            super.visitJumpInsn(opcode, label);
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);

            if (stage == SEARCH_JMP_TARGET && label == jumpTarget) {
                callHook(true);
                super.visitVarInsn(ASTORE, 8);

                stage = DONE;
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == RETURN) {
                callHook(false);
            }
            super.visitInsn(opcode);
        }

        private void callHook(boolean pre) {
            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(ALOAD, 8);
            // side <= 5, so we have ICONST_N for all of them
            super.visitInsn(ICONST_0 + side);

            Type iiconType = Type.getObjectType("net/minecraft/util/IIcon");
            Type renderBlocksType = Type.getObjectType("net/minecraft/client/renderer/RenderBlocks");

            String desc = Type.getMethodDescriptor(pre ? iiconType : Type.VOID_TYPE, renderBlocksType, iiconType, Type.INT_TYPE);
            super.visitMethodInsn(INVOKESTATIC, ASMHooks.CLASS_NAME, pre ? ASMHooks.PRE_RENDER_BLOCK : ASMHooks.POST_RENDER_BLOCK, desc, false);
        }
    }
}
