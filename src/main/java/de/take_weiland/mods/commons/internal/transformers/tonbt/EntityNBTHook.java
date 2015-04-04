package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.VOID_TYPE;

/**
 * @author diesieben07
 */
public final class EntityNBTHook extends ClassVisitor {

    public EntityNBTHook(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(MCPNames.M_ENTITY_WRITE_NBT))) {
            return new Hook(mv, ASMHooks.WRITE_NBT_HOOK);
        } else if (name.equals(MCPNames.method(MCPNames.M_ENTITY_READ_NBT))) {
            return new Hook(mv, ASMHooks.READ_NBT_HOOK);
        } else {
            return mv;
        }
    }

    private static final class Hook extends MethodVisitor {

        private final String targetMethodName;
        private boolean done = false;

        private Hook(MethodVisitor mv, String targetMethodName) {
            super(ASM4, mv);
            this.targetMethodName = targetMethodName;
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);

            if (!done) {
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 1);

                Type objectType = Type.getType(Object.class);
                Type nbtCompType = Type.getObjectType("net/minecraft/nbt/NBTTagCompound");

                String owner = Type.getInternalName(ASMHooks.class);
                String desc = Type.getMethodDescriptor(VOID_TYPE, objectType, nbtCompType);
                super.visitMethodInsn(INVOKESTATIC, owner, targetMethodName, desc);
                done = true;
            }
        }
    }


}
