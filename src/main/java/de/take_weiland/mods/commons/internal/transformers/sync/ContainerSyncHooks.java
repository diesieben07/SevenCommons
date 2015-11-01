package de.take_weiland.mods.commons.internal.transformers.sync;

import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.SRGConstants;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public final class ContainerSyncHooks extends ClassVisitor {

    public ContainerSyncHooks(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (name.equals(MCPNames.method(SRGConstants.M_DETECT_AND_SEND_CHANGES))) {
            return new MethodTransformer(mv);
        } else {
            return mv;
        }
    }

    private static final class MethodTransformer extends MethodVisitor {

        MethodTransformer(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();

            String containerIntName = "net/minecraft/inventory/Container";
            Type syncerCompanionType = Type.getType(SyncCompanion.class);
            super.visitVarInsn(ALOAD, 0);
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, containerIntName, CompanionFieldAdder.COMPANION_FIELD, syncerCompanionType.getDescriptor());

            String hookClazz = Type.getInternalName(ASMHooks.class);
            String invokeCheck = ASMHooks.INVOKE_SYNC_COMP_CHECK;
            String invokeCheckDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class), syncerCompanionType);
            super.visitMethodInsn(INVOKESTATIC, hookClazz, invokeCheck, invokeCheckDesc, false);

            super.visitVarInsn(ALOAD, 0);
            String desc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(containerIntName));
            super.visitMethodInsn(INVOKESTATIC, hookClazz, ASMHooks.TICK_CONTAINER_COMPANIONS, desc, false);
        }
    }
}
