package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.client.worldview.WorldViewImpl;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class MinecraftHook extends ClassVisitor {

    static final         String MINECRAFT_CLASS    = "net/minecraft/client/Minecraft";
    private static final String WORLD_CLIENT_CLASS = "net/minecraft/client/multiplayer/WorldClient";

    public MinecraftHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_MINECRAFT_LOAD_WORLD)) && desc.equals(Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(WORLD_CLIENT_CLASS), Type.getType(String.class)))) {
            mv = new LoadWorldHook(mv);
        }
        return mv;
    }

    private static final class LoadWorldHook extends MethodVisitor {

        private static final int START = 0, FOUND_LCONST = 1, FOUND_PUTFIELD = 2, DONE = 3;

        private int stage = START;

        LoadWorldHook(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (stage == START && opcode == LCONST_0) {
                stage = FOUND_LCONST;
            } else if (stage == FOUND_PUTFIELD && opcode == RETURN) {
                super.visitVarInsn(ALOAD, 1); // world param
                super.visitMethodInsn(INVOKESTATIC, WorldViewImpl.CLASS_NAME, WorldViewImpl.SET_CLIENT_WORLD, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(WORLD_CLIENT_CLASS)), false);
                stage = DONE;
            }

            super.visitInsn(opcode);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (stage == FOUND_LCONST && opcode == PUTFIELD && owner.equals(MINECRAFT_CLASS) && name.equals(MCPNames.field(SRGConstants.F_MINECRAFT_SYSTEM_TIME))) {
                stage = FOUND_PUTFIELD;
            } else {
                stage = START;
            }

            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }
}
