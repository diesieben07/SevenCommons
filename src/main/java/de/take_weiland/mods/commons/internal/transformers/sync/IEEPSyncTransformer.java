package de.take_weiland.mods.commons.internal.transformers.sync;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.sync.IEEPSyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncCompanion;
import de.take_weiland.mods.commons.internal.sync.SyncedObjectProxy;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class IEEPSyncTransformer extends ClassVisitor {

    private static final String IEEP = "net/minecraftforge/common/IExtendedEntityProperties";

    public IEEPSyncTransformer(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (interfaces == null) {
            interfaces = new String[]{SyncedObjectProxy.CLASS_NAME};
        } else {
            interfaces = ObjectArrays.concat(SyncedObjectProxy.CLASS_NAME, interfaces);
        }
        super.visit(V1_8, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, SyncedObjectProxy.GET_COMPANION, Type.getMethodDescriptor(Type.getType(SyncCompanion.class)), null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, ASMHooks.CLASS_NAME, ASMHooks.GET_IEEP_COMPANION,
                    Type.getMethodDescriptor(Type.getType(IEEPSyncCompanion.class), Type.getObjectType(IEEP)), true);

            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
