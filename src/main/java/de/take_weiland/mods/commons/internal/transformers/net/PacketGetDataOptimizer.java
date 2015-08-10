package de.take_weiland.mods.commons.internal.transformers.net;

import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import de.take_weiland.mods.commons.internal.net.PacketToChannelMap;
import de.take_weiland.mods.commons.internal.net.SimplePacketData;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class PacketGetDataOptimizer extends ClassVisitor {

    public PacketGetDataOptimizer(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        if ((access & ACC_INTERFACE) == 0
                && (access & ACC_ABSTRACT) == 0
                && !name.equals("de/take_weiland/mods/commons/internal/net/BaseModPacket")
                && ClassInfo.of(BaseModPacket.class).isAssignableFrom(ClassInfo.of(name))) {
            FieldVisitor fv = super.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, PacketToChannelMap.PACKET_DATA_FIELD, Type.getDescriptor(SimplePacketData.class), null, null);
            if (fv != null) {
                fv.visitEnd();
            }

            MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, "_sc$getData", Type.getMethodDescriptor(Type.getType(SimplePacketData.class)), null, null);
            if (mv != null) {
                mv.visitCode();

                mv.visitFieldInsn(GETSTATIC, name, PacketToChannelMap.PACKET_DATA_FIELD, Type.getDescriptor(SimplePacketData.class));
                mv.visitInsn(ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
        }
    }

}
