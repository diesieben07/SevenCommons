package de.take_weiland.mods.commons.internal.transformers.sync;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.sync.SyncerCompanion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class TileEntityTickHook extends ClassVisitor {

    public TileEntityTickHook(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(MCPNames.M_UPDATE_ENTITIES))) {
            return new UpdateEntitiesTransformer(mv);
        } else {
            return mv;
        }
    }

    private static final class UpdateEntitiesTransformer extends MethodVisitor {

        private int lastLoadLocal = -1;

        UpdateEntitiesTransformer(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (opcode == ALOAD) {
                lastLoadLocal = var;
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            super.visitMethodInsn(opcode, owner, name, desc);
            String teIntName = "net/minecraft/tileentity/TileEntity";
            Type worldType = Type.getObjectType("net/minecraft/world/World");

            if (owner.equals(teIntName) && name.equals(MCPNames.method(MCPNames.M_UPDATE_ENTITY))) {
                boolean client = FMLLaunchHandler.side().isClient();

                Label after = new Label();
                if (client) {
                    super.visitVarInsn(ALOAD, 0);
                    super.visitFieldInsn(GETFIELD, worldType.getInternalName(), MCPNames.field(MCPNames.F_IS_REMOTE), Type.BOOLEAN_TYPE.getDescriptor());
                    super.visitJumpInsn(IFNE, after);
                }

                super.visitVarInsn(ALOAD, lastLoadLocal);
                super.visitVarInsn(ALOAD, lastLoadLocal);
                super.visitFieldInsn(GETFIELD, teIntName, CompanionFieldAdder.COMPANION_FIELD, Type.getDescriptor(SyncerCompanion.class));

                String hookClazz = Type.getInternalName(ASMHooks.class);
                String invokeCheck = ASMHooks.INVOKE_SYNC_COMP_CHECK;
                String invokeCheckDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), Type.getType(SyncerCompanion.class));
                super.visitMethodInsn(INVOKESTATIC, hookClazz, invokeCheck, invokeCheckDesc);

                if (client) {
                    super.visitLabel(after);
                }
            }
        }
    }
}
