package de.take_weiland.mods.commons.internal.transformers;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.WorldProxy;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class WorldHook extends ClassVisitor {

    static final         String WORLD_CLASS_NAME    = "net/minecraft/world/World";
    private static final String PROFILER_CLASS_NAME = "net/minecraft/profiler/Profiler";

    WorldHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, Type.getInternalName(WorldProxy.class));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        String mDesc = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE);
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, WorldProxy.DOES_CHUNK_EXIST, mDesc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, WORLD_CLASS_NAME, MCPNames.method(SRGConstants.M_WORLD_CHUNK_EXISTS), mDesc, false);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_WORLD_SET_PLAYER_ACTIVE_CHUNKS))) {
            return new PlayerActiveChunksHook(mv);
        }
        return mv;
    }

    private static final class PlayerActiveChunksHook extends MethodVisitor {

        private static final int START = 0, FOUND_GETFIELD = 1, FOUND_LDC = 2, DONE = 3;

        private int state = START;

        PlayerActiveChunksHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (state == START && opcode == GETFIELD && owner.equals(WORLD_CLASS_NAME) && name.equals(MCPNames.field(SRGConstants.F_WORLD_THE_PROFILER))) {
                state = FOUND_GETFIELD;
            } else {
                state = START;
            }
        }

        @Override
        public void visitLdcInsn(Object cst) {
            super.visitLdcInsn(cst);
            if (state == FOUND_GETFIELD && cst.equals("buildList")) {
                state = FOUND_LDC;
            } else {
                state = START;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (state == FOUND_LDC && opcode == INVOKEVIRTUAL && owner.equals(PROFILER_CLASS_NAME)) {
                Label label;
                if (FMLLaunchHandler.side().isClient()) {
                    label = new Label();
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, WORLD_CLASS_NAME, MCPNames.field(SRGConstants.F_WORLD_IS_REMOTE), Type.BOOLEAN_TYPE.getDescriptor());
                    super.visitJumpInsn(IFNE, label);
                } else {
                    label = null;
                }

                super.visitVarInsn(ALOAD, 0);
                super.visitFieldInsn(GETFIELD, WORLD_CLASS_NAME, MCPNames.field(SRGConstants.F_WORLD_ACTIVE_CHUNKS), Type.getDescriptor(Set.class));
                super.visitMethodInsn(INVOKESTATIC, ServerChunkViewManager.CLASS_NAME, ServerChunkViewManager.ENHANCE_ACTIVE_CHUNK_SET, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Set.class)), false);

                if (label != null) {
                    super.visitLabel(label);
                }

                state = DONE;
            } else {
                state = START;
            }
        }
    }

}
