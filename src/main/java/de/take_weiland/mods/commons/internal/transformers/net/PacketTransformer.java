package de.take_weiland.mods.commons.internal.transformers.net;

import com.google.common.collect.ObjectArrays;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class PacketTransformer extends ClassVisitor {

    public PacketTransformer(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (interfaces == null || interfaces.length == 0) {
            interfaces = new String[]{BaseNettyPacket.CLASS_NAME};
        } else {
            interfaces = ObjectArrays.concat(BaseNettyPacket.CLASS_NAME, interfaces);
        }
        super.visit(version, access, name, signature, superName, interfaces);

        String networkImpl = "de/take_weiland/mods/commons/internal/net/NetworkImpl";

        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, BaseNettyPacket.HANDLE,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(EntityPlayer.class)),
                null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, networkImpl, NetworkImpl.HANDLE_SIMPLE_PACKET,
                    Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(name), Type.getType(EntityPlayer.class)), false);
            mv.visitInsn(RETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = super.visitMethod(ACC_PUBLIC, BaseNettyPacket.ENCODE,
                Type.getMethodDescriptor(Type.getType(net.minecraft.network.Packet.class)),
                null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, networkImpl, FMLLaunchHandler.side().isClient() ? NetworkImpl.MAKE_PKT_TO_SERVER : NetworkImpl.MAKE_PKT_TO_SERVER_INV,
                    Type.getMethodDescriptor(Type.getType(net.minecraft.network.Packet.class), Type.getObjectType(name)), false);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        mv = super.visitMethod(ACC_PUBLIC, BaseNettyPacket.ENCODE_PLAYER,
                Type.getMethodDescriptor(Type.getType(net.minecraft.network.Packet.class)),
                null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, networkImpl, NetworkImpl.MAKE_PKT_TO_CLIENT,
                    Type.getMethodDescriptor(Type.getType(net.minecraft.network.Packet.class), Type.getObjectType(name)), false);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
