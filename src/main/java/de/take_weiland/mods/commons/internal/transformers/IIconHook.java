package de.take_weiland.mods.commons.internal.transformers;

import de.take_weiland.mods.commons.internal.IconProviderInternal;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class IIconHook extends ClassVisitor {

    IIconHook(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        interfaces = ArrayUtils.add(interfaces, IconProviderInternal.CLASS_NAME);

        super.visit(V1_8, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        String desc = Type.getMethodDescriptor(Type.getObjectType("net/minecraft/util/IIcon"), Type.INT_TYPE);
        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, IconProviderInternal.GET_ICON, desc, null, null);
        if (mv != null) {
            mv.visitCode();

            mv.visitVarInsn(ALOAD, 0);
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
