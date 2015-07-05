package de.take_weiland.mods.commons.internal.transformers.sync;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class EntityTickHook extends ClassVisitor {

    public EntityTickHook(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_WORLD_UPDATE_ENTITY_WITH_OPTIONAL_FORCE))) {
            return new UpdateEntityTransformer(mv);
        } else {
            return mv;
        }
    }

    private static final class UpdateEntityTransformer extends MethodVisitor {

        private boolean foundIAdd = false;
        private boolean done = false;

        UpdateEntityTransformer(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            super.visitInsn(opcode);
            foundIAdd = !done && opcode == IADD;
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);

            String entityIntName = "net/minecraft/entity/Entity";
            if (foundIAdd && owner.equals(entityIntName) && name.equals(MCPNames.field(SRGConstants.F_ENTITY_TICKS_EXISTED))) {
                Type syncerCompanionType = Type.getType(SyncCompanion.class);
                Type worldType = Type.getObjectType("net/minecraft/world/World");

                boolean client = FMLLaunchHandler.side().isClient();

                Label after = new Label();
                if (client) {
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, worldType.getInternalName(), MCPNames.field(SRGConstants.F_IS_REMOTE), Type.BOOLEAN_TYPE.getDescriptor());
                    super.visitJumpInsn(IFNE, after);
                }

                super.visitVarInsn(ALOAD, 1); // entity parameter
                super.visitVarInsn(ALOAD, 1);
                super.visitFieldInsn(GETFIELD, entityIntName, CompanionFieldAdder.COMPANION_FIELD, syncerCompanionType.getDescriptor());

                String hookClazz = Type.getInternalName(ASMHooks.class);
                String invokeCheck = ASMHooks.INVOKE_SYNC_COMP_CHECK;
                String invokeCheckDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(SyncCompanion.class));
                super.visitMethodInsn(INVOKESTATIC, hookClazz, invokeCheck, invokeCheckDesc, false);

                if (client) {
                    super.visitLabel(after);
                }

                done = true;
            }

            foundIAdd = false;
        }
    }

}
