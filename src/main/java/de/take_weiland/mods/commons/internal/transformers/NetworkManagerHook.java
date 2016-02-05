package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.worldview.VanillaPacketPrefixes;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class NetworkManagerHook extends ClassVisitor {

    static final String NETWORK_MANAGER_CLASS = "net/minecraft/network/NetworkManager";

    NetworkManagerHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_PROCESS_RECEIVED_PACKETS))) {
            mv = new MethodHook(mv);
        }
        return mv;
    }

    private static final class MethodHook extends MethodVisitor {

        private int lastNonThisAload = -1;

        MethodHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);

            if (opcode == ALOAD && var != 0) {
                lastNonThisAload = var;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            boolean doHook = false;
            String mDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.BOOLEAN_TYPE, Type.getObjectType(PacketEncoderHook.PACKET));

            if (opcode == INVOKEVIRTUAL && owner.equals(PacketEncoderHook.PACKET) && name.equals(MCPNames.method(SRGConstants.M_PROCESS_PACKET))) {
                if (lastNonThisAload == -1) {
                    throw new RuntimeException("Failed to hook into NetworkManager.processPackets");
                } else {
                    doHook = true;
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, NETWORK_MANAGER_CLASS, MCPNames.field(SRGConstants.F_NETWORK_MANAGER_CLIENTSIDE), Type.BOOLEAN_TYPE.getDescriptor());
                    super.visitVarInsn(ALOAD, lastNonThisAload);
                    super.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.PRE_PACKET_PROCESS, mDesc, false);
                }
            }

            super.visitMethodInsn(opcode, owner, name, desc, itf);

            if (doHook) {
                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, NETWORK_MANAGER_CLASS, MCPNames.field(SRGConstants.F_NETWORK_MANAGER_CLIENTSIDE), Type.BOOLEAN_TYPE.getDescriptor());
                super.visitVarInsn(ALOAD, lastNonThisAload);
                super.visitMethodInsn(INVOKESTATIC, VanillaPacketPrefixes.CLASS_NAME, VanillaPacketPrefixes.POST_PACKET_PROCESS, mDesc, false);
            }
        }
    }
}
