package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.worldview.VanillaPacketPrefixes;
import de.take_weiland.mods.commons.internal.worldview.VanillaPacketProxy;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class PacketEncoderHook extends ClassVisitor {

    static final String MESSAGE_SERIALIZER      = "net/minecraft/util/MessageSerializer";
    static final String CHANNEL_HANDLER_CONTEXT = "io/netty/channel/ChannelHandlerContext";
    static final String PACKET                  = "net/minecraft/network/Packet";
    static final String BYTE_BUF                = "io/netty/buffer/ByteBuf";
    static final String PACKET_BUF              = "net/minecraft/network/PacketBuffer";

    PacketEncoderHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals("encode") && desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(CHANNEL_HANDLER_CONTEXT), Type.getObjectType(PACKET), Type.getObjectType(BYTE_BUF)))) {
            mv = new EncodeHook(mv);
        }
        return mv;
    }

    private static final class EncodeHook extends MethodVisitor {

        private static final int START = 0, FOUND_PB_INIT = 1, DID_PREFIX = 2, DONE = 3;
        private int state = START;
        private int packetBufSlot;

        EncodeHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);

            if (state == START && opcode == INVOKESPECIAL && owner.equals(PACKET_BUF) && name.equals("<init>")) {
                state = FOUND_PB_INIT;
            } else if (state == DID_PREFIX && opcode == INVOKEVIRTUAL && owner.equals(PACKET_BUF) && name.equals(MCPNames.method(SRGConstants.M_WRITE_VARINT_TO_BUFFER))) {
                mv.visitVarInsn(ALOAD, packetBufSlot);
                mv.visitVarInsn(ALOAD, 2);
                String mDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(PACKET_BUF), Type.getType(VanillaPacketProxy.class));
                mv.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.WRITE_DIM_ID, mDesc, false);
                state = DONE;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);

            if (state == FOUND_PB_INIT && opcode == ASTORE) {
                packetBufSlot = var;

                super.visitVarInsn(ALOAD, var);
                super.visitVarInsn(ALOAD, 2); // packet is 2nd parameter
                String mDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(PACKET_BUF), Type.getType(VanillaPacketProxy.class));
                super.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.WRITE_PREFIX, mDesc, false);

                state = DID_PREFIX;
            }
        }
    }
}
