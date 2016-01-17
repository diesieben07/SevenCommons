package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.PlayerManagerProxy;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class PlayerManagerHook extends ClassVisitor {

    static final         String PLAYER_MANAGER_CLASS       = "net/minecraft/server/management/PlayerManager";
    static final         String PLAYER_INSTANCE_CLASS      = "net/minecraft/server/management/PlayerManager$PlayerInstance";
    static final         String CHUNK_COORD_INT_PAIR_CLASS = "net/minecraft/world/ChunkCoordIntPair";
    private static final String CHUNK_CLASS_NAME           = "net/minecraft/world/chunk/Chunk";

    PlayerManagerHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, Type.getInternalName(PlayerManagerProxy.class));
        super.visit(V1_8, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        String desc = Type.getMethodDescriptor(Type.getType(PlayerManagerProxy.PlayerInstanceAdapter.class), Type.INT_TYPE, Type.INT_TYPE, Type.BOOLEAN_TYPE);
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, PlayerManagerProxy.GET_PLAYER_INSTANCE, desc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitVarInsn(ILOAD, 3);
            String d = Type.getMethodDescriptor(Type.getObjectType(PLAYER_INSTANCE_CLASS), Type.INT_TYPE, Type.INT_TYPE, Type.BOOLEAN_TYPE);
            mv.visitMethodInsn(INVOKESPECIAL, PLAYER_MANAGER_CLASS, MCPNames.method(SRGConstants.M_GET_OR_CREATE_CHUNK_WATCHER), d, false);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }

    static final class PlayerInstanceHook extends ClassVisitor {

        PlayerInstanceHook(ClassVisitor cv) {
            super(ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            interfaces = ArrayUtils.add(interfaces, Type.getInternalName(PlayerManagerProxy.PlayerInstanceAdapter.class));
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public void visitEnd() {
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, PlayerManagerProxy.PlayerInstanceAdapter.GET_PLAYERS_WATCHING, Type.getMethodDescriptor(Type.getType(List.class)), null, null);
            if (mv != null) {
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, PLAYER_INSTANCE_CLASS, MCPNames.field(SRGConstants.F_PLAYERS_WATCHING_CHUNK), Type.getDescriptor(List.class));
                mv.visitInsn(ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }
            super.visitEnd();
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
            if (name.equals(MCPNames.method(SRGConstants.M_PLAYER_INSTANCE_REMOVE_PLAYER))) {
                return new RemovePlayerPatcher(mv);
            }
            return mv;
        }
    }

    static final class RemovePlayerPatcher extends MethodVisitor {

        private int     lastAloadSlot = -1;
        private boolean foundInvoke   = false;

        RemovePlayerPatcher(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (opcode == ALOAD) {
                lastAloadSlot = var;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            foundInvoke = opcode == INVOKEVIRTUAL && owner.equals(CHUNK_CLASS_NAME) && name.equals(MCPNames.method(SRGConstants.M_CHUNK_IS_POPULATED));
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            super.visitJumpInsn(opcode, label);
            if (foundInvoke && opcode == IFEQ && lastAloadSlot != -1) {
                super.visitVarInsn(ALOAD, 1); // player
                super.visitVarInsn(ALOAD, lastAloadSlot); // chunk
                String mDesc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getObjectType("net/minecraft/entity/player/EntityPlayerMP"), Type.getObjectType(CHUNK_CLASS_NAME));
                super.visitMethodInsn(INVOKESTATIC, Type.getInternalName(ServerChunkViewManager.class), ServerChunkViewManager.SUPPRESS_UNLOAD_PACKET, mDesc, false);
                super.visitJumpInsn(IFNE, label); // also skip over packet sending code when suppress returns true
            }
        }
    }

}
