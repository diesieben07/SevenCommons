package de.take_weiland.mods.commons.internal.transformers;

import com.google.common.collect.ObjectArrays;
import de.take_weiland.mods.commons.asm.info.ClassInfo;
import de.take_weiland.mods.commons.internal.ListenableInternal;
import de.take_weiland.mods.commons.util.Listenable;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public class ListenableSupport extends ClassVisitor {

    private String className;

    public ListenableSupport(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String listenableName = Type.getInternalName(Listenable.class);
        if ((access & ACC_INTERFACE) == 0 && ArrayUtils.contains(ArrayUtils.nullToEmpty(interfaces), listenableName)) {
            if (!ClassInfo.of(Listenable.class).isAssignableFrom(ClassInfo.of(superName))) {
                interfaces = ObjectArrays.concat(Type.getInternalName(ListenableInternal.class), interfaces);
                className = name;
            }
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        if (className != null) {
            String fDesc = Type.getDescriptor(List.class);
            String fName = "_sc$lst";

            FieldVisitor fv = super.visitField(ACC_PRIVATE, fName, fDesc, null, null);
            if (fv != null) {
                fv.visitEnd();
            }
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, ListenableInternal.GET, Type.getMethodDescriptor(Type.getType(List.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, className, fName, fDesc);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();

            mv = super.visitMethod(ACC_PUBLIC | ACC_FINAL, ListenableInternal.SET, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(List.class)), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, className, fName, fDesc);
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        super.visitEnd();
    }
}
