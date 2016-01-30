package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class ServerConfigManagerHook extends ClassVisitor {

    private static final String NET_HANDLER_PLAY_SERVER = "net/minecraft/network/NetHandlerPlayServer";
    private static final String ENTITY_PLAYER_MP        = "net/minecraft/entity/player/EntityPlayerMP";

    ServerConfigManagerHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_SEND_TO_ALL_NEAR_EXCEPT))) {
            mv = new SendNearHook(mv);
        }
        return mv;
    }

    private static final class SendNearHook extends MethodVisitor {

        private static final int START = 0, FOUND_CHECKCAST = 1, FOUND_ASTORE = 2, FOUND_JMP = 3, FOUND_INVOKE = 4, DONE = 5;
        private int state = START;
        private int   playerSlot;
        private Label lbl;

        SendNearHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            super.visitTypeInsn(opcode, type);

            if (state == START && opcode == CHECKCAST && type.equals(ENTITY_PLAYER_MP)) {
                state = FOUND_CHECKCAST;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);

            if (state == FOUND_CHECKCAST && opcode == ASTORE) {
                state = FOUND_ASTORE;
                playerSlot = var;
            }
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);

            if (state == FOUND_ASTORE && opcode == IFGE) {
                lbl = label;
                state = FOUND_JMP;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (state == FOUND_JMP && opcode == INVOKEVIRTUAL && owner.equals(NET_HANDLER_PLAY_SERVER) && name.equals(MCPNames.method(SRGConstants.M_NET_HANDLER_PLAY_SERVER_SEND_PACKET))) {
                state = FOUND_INVOKE;
            }
        }

        @Override
        public void visitLabel(Label label) {
            if (state == FOUND_INVOKE) {
                Label after = new Label();
                super.visitJumpInsn(GOTO, after);
                super.visitLabel(label);

                super.visitVarInsn(ALOAD, playerSlot);
                for (int i = 2; i < 9; i += 2) {
                    super.visitVarInsn(DLOAD, i);
                }
                super.visitVarInsn(ILOAD, 10);
                super.visitVarInsn(ALOAD, 11);
                String mDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(ENTITY_PLAYER_MP), Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.INT_TYPE, Type.getObjectType(PacketEncoderHook.PACKET));
                super.visitMethodInsn(INVOKESTATIC, ServerChunkViewManager.CLASS_NAME, ServerChunkViewManager.SEND_NEAR_HOOK, mDesc, false);

                super.visitLabel(after);

                state = DONE;
            } else {
                super.visitLabel(label);
            }
        }
    }
}
