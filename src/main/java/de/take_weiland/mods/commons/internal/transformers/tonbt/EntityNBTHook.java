package de.take_weiland.mods.commons.internal.transformers.tonbt;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import net.minecraftforge.common.IExtendedEntityProperties;
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
            return new Hook(mv, ASMHooks.WRITE_NBT_HOOK, "saveNBTData", ASMHooks.IEEP_WRITE_NBT_HOOK);
        } else if (name.equals(MCPNames.method(MCPNames.M_ENTITY_READ_NBT))) {
            return new Hook(mv, ASMHooks.READ_NBT_HOOK, "loadNBTData", ASMHooks.IEEP_READ_NBT_HOOK);
        } else {
            return mv;
        }
    }

    private static final class Hook extends MethodVisitor {

        private final String entityHookMethod;
        private final String ieepMethod;
        private final String ieepHookMethod;
        private int prevLastSavedVar;
        private int lastSavedVar;
        private boolean didEntityHook = false;

        private Hook(MethodVisitor mv, String entityHookMethod, String ieepMethod, String ieepHookMethod) {
            super(ASM4, mv);
            this.entityHookMethod = entityHookMethod;
            this.ieepMethod = ieepMethod;
            this.ieepHookMethod = ieepHookMethod;
        }

        @Override
        public void visitLabel(Label label) {
            super.visitLabel(label);
            // first label is of the try-catch

            if (!didEntityHook) {
                super.visitVarInsn(ALOAD, 0);
                super.visitVarInsn(ALOAD, 1);

                Type objectType = Type.getType(Object.class);
                Type nbtCompType = Type.getObjectType("net/minecraft/nbt/NBTTagCompound");
                Type asmHooksType = Type.getType(ASMHooks.class);

                super.visitMethodInsn(INVOKESTATIC,
                        asmHooksType.getInternalName(),
                        entityHookMethod,
                        Type.getMethodDescriptor(VOID_TYPE, objectType, nbtCompType), false);

                didEntityHook = true;
            }
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (opcode == ASTORE) {
                // need the last two ASTORE vars, they are identifier and ieep
                prevLastSavedVar = lastSavedVar;
                lastSavedVar = var;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == INVOKEINTERFACE
                    && owner.equals(Type.getInternalName(IExtendedEntityProperties.class))
                    && name.equals(ieepMethod)) {

                Type objectType = Type.getType(Object.class);
                Type stringType = Type.getType(String.class);
                Type nbtCompType = Type.getObjectType("net/minecraft/nbt/NBTTagCompound");

                super.visitVarInsn(ALOAD, lastSavedVar);
                super.visitVarInsn(ALOAD, prevLastSavedVar);
                super.visitVarInsn(ALOAD, 1);

                super.visitMethodInsn(INVOKESTATIC,
                        Type.getInternalName(ASMHooks.class),
                        ieepHookMethod,
                        Type.getMethodDescriptor(VOID_TYPE, objectType, stringType, nbtCompType), false);
            }
        }

    }


}
