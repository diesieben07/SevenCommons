package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.worldview.VanillaPacketPrefixes;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static de.take_weiland.mods.commons.internal.transformers.PacketEncoderHook.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class PacketDecoderHook extends ClassVisitor {

    static final String MESSAGE_DESERIALIZER = "net/minecraft/util/MessageDeserializer";

    PacketDecoderHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("decode")) {
            mv = new DecodeHook(mv);
        }
        return mv;
    }

    private static final class DecodeHook extends MethodVisitor {

        private static final int START = 0, FOUND_ALOAD = 1, FOUND_INVOKE = 2, FOUND_ISTORE = 3, DONE = 4;
        private int state = START;
        private int packetBufferSlot;

        private Label hadPrefixCreate;

        DecodeHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);

            if ((state == START || state == FOUND_ALOAD) && opcode == ALOAD) {
                packetBufferSlot = var;
                state = FOUND_ALOAD;
            } else if (state == FOUND_INVOKE && opcode == ISTORE) {
                Label dontHavePrefix = new Label();
                hadPrefixCreate = new Label();

                super.visitVarInsn(ILOAD, var);
                super.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.IS_PREFIX_ID, Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.INT_TYPE), false);
                super.visitJumpInsn(IFEQ, dontHavePrefix);

                // read actual packetID
                super.visitVarInsn(ALOAD, packetBufferSlot);
                super.visitMethodInsn(INVOKEVIRTUAL, PACKET_BUF, MCPNames.method(SRGConstants.M_READ_VARINT_FROM_BUFFER), Type.getMethodDescriptor(Type.INT_TYPE), false);
                super.visitVarInsn(ISTORE, var);

                // hand off creation to our class and skip over vanilla code
                super.visitVarInsn(ALOAD, 1); // load ChannelHandlerContext
                super.visitVarInsn(ALOAD, packetBufferSlot);
                super.visitVarInsn(ILOAD, var);
                String mDesc = Type.getMethodDescriptor(Type.getObjectType(PACKET), Type.getObjectType(CHANNEL_HANDLER_CONTEXT), Type.getObjectType(PACKET_BUF), Type.INT_TYPE);
                super.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.READ_WITH_PREFIX, mDesc, false);

                super.visitJumpInsn(GOTO, hadPrefixCreate);

                super.visitLabel(dontHavePrefix);

                state = FOUND_ISTORE;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);

            if (state == FOUND_ALOAD && owner.equals(PACKET_BUF) && name.equals(MCPNames.method(SRGConstants.M_READ_VARINT_FROM_BUFFER))) {
                state = FOUND_INVOKE;
            } else if (state == FOUND_ISTORE && opcode == INVOKESTATIC && owner.equals(PACKET) && name.equals(MCPNames.method(SRGConstants.M_CREATE_PACKET))) {
                super.visitLabel(hadPrefixCreate);
                state = DONE;
            }
        }
    }
}
